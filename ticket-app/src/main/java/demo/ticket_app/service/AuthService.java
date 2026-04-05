package demo.ticket_app.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import demo.ticket_app.dto.auth.AuthResponse;
import demo.ticket_app.dto.auth.GoogleAuthPayload;
import demo.ticket_app.dto.auth.GoogleAuthRequest;
import demo.ticket_app.dto.auth.LoginRequest;
import demo.ticket_app.dto.auth.RegisterRequest;
import demo.ticket_app.entity.User;
import demo.ticket_app.entity.UserRole;
import demo.ticket_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    @Value("${google.oauth.client-id:${GOOGLE_OAUTH_CLIENT_ID:${GOOGLE_CLIENT_ID:}}}")
    private String googleOAuthClientId;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already registered");
        }

        User user = User.builder()
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .phone(request.phone())
                .role(UserRole.CUSTOMER)
                .isActive(true)
                .isVerified(false)
                .build();

        User savedUser = userRepository.save(user);
        String token = jwtService.generateToken(savedUser);

        return new AuthResponse(
                token,
                "Bearer",
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getFullName(),
                savedUser.getRole()
        );
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizedEmail, request.password())
            );
        } catch (AuthenticationException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        User user = userRepository.findByEmail(normalizedEmail)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is inactive");
        }

        String token = jwtService.generateToken(user);
        return new AuthResponse(
                token,
                "Bearer",
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole()
        );
    }

    @Transactional
    public AuthResponse loginWithGoogle(GoogleAuthRequest request) {
        GoogleAuthPayload payload = verifyGoogleToken(request.idToken());
        String normalizedEmail = payload.email().trim().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail)
                .map(existing -> syncGoogleProfile(existing, payload))
                .orElseGet(() -> createGoogleUser(payload));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is inactive");
        }

        String token = jwtService.generateToken(user);
        return new AuthResponse(
                token,
                "Bearer",
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole()
        );
    }

    private GoogleAuthPayload verifyGoogleToken(String idTokenString) {
        if (googleOAuthClientId == null || googleOAuthClientId.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Google login is temporarily unavailable: GOOGLE_OAUTH_CLIENT_ID is not configured"
            );
        }

        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleOAuthClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Google token");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            if (email == null || email.isBlank()) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Google account email is missing");
            }

            Map<String, Object> claims = parseJwtClaims(idTokenString);
            boolean emailVerified = resolveEmailVerified(payload.getEmailVerified(), claims.get("email_verified"));
            String name = resolveName(claims, email);
            String picture = resolveStringClaim(claims, "picture");

            return new GoogleAuthPayload(email, name, picture, emailVerified);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (GeneralSecurityException | IOException | RuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Cannot verify Google token");
        }
    }

    private Map<String, Object> parseJwtClaims(String idTokenString) throws IOException {
        String[] segments = idTokenString.split("\\.");
        if (segments.length < 2) {
            return Collections.emptyMap();
        }

        byte[] decoded = Base64.getUrlDecoder().decode(segments[1]);
        String payloadJson = new String(decoded, StandardCharsets.UTF_8);
        return objectMapper.readValue(payloadJson, new TypeReference<Map<String, Object>>() {});
    }

    private boolean resolveEmailVerified(Boolean payloadEmailVerified, Object claimEmailVerified) {
        if (payloadEmailVerified != null) {
            return payloadEmailVerified;
        }
        if (claimEmailVerified instanceof Boolean boolValue) {
            return boolValue;
        }
        if (claimEmailVerified instanceof String stringValue) {
            return Boolean.parseBoolean(stringValue);
        }
        return false;
    }

    private String resolveName(Map<String, Object> claims, String fallbackEmail) {
        String fullName = resolveStringClaim(claims, "name");
        if (fullName != null && !fullName.isBlank()) {
            return fullName;
        }

        String givenName = resolveStringClaim(claims, "given_name");
        String familyName = resolveStringClaim(claims, "family_name");
        String joined = ((givenName == null ? "" : givenName) + " " + (familyName == null ? "" : familyName)).trim();
        if (!joined.isBlank()) {
            return joined;
        }

        return fallbackEmail;
    }

    private String resolveStringClaim(Map<String, Object> claims, String key) {
        Object value = claims.get(key);
        return value instanceof String stringValue ? stringValue : null;
    }

    private User syncGoogleProfile(User existing, GoogleAuthPayload payload) {
        existing.setFullName(payload.fullName() != null && !payload.fullName().isBlank() ? payload.fullName() : existing.getFullName());
        existing.setAvatarUrl(payload.avatarUrl());
        if (payload.emailVerified()) {
            existing.setIsVerified(true);
        }
        if (existing.getRole() == null) {
            existing.setRole(UserRole.CUSTOMER);
        }
        if (existing.getIsActive() == null) {
            existing.setIsActive(true);
        }
        return userRepository.save(existing);
    }

    private User createGoogleUser(GoogleAuthPayload payload) {
        User user = User.builder()
                .id(UUID.randomUUID())
                .email(payload.email().trim().toLowerCase())
                .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                .fullName(payload.fullName())
                .avatarUrl(payload.avatarUrl())
                .role(UserRole.CUSTOMER)
                .isActive(true)
                .isVerified(payload.emailVerified())
                .build();
        return userRepository.save(user);
    }
}
