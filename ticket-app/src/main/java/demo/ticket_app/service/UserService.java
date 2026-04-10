package demo.ticket_app.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import demo.ticket_app.dto.auth.CreateStaffAccountRequest;
import demo.ticket_app.dto.auth.CreateStaffAccountResponse;
import demo.ticket_app.entity.User;
import demo.ticket_app.entity.UserRole;
import demo.ticket_app.exception.ResourceNotFoundException;
import demo.ticket_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(UUID userId) {
        UUID safeUserId = Objects.requireNonNull(userId, "userId must not be null");
        return userRepository.findById(safeUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    public List<User> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    public List<User> getActiveUsers() {
        return userRepository.findByIsActive(true);
    }

    public List<User> getInactiveUsers() {
        return userRepository.findByIsActive(false);
    }

    public User createUser(User user) {
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setIsActive(true);
        
        User savedUser = userRepository.save(user);
        log.info("Created new user with id: {}", savedUser.getId());
        return savedUser;
    }

    public User updateUser(UUID userId, User userDetails) {
        User existingUser = getUserById(userId);
        
        existingUser.setFullName(userDetails.getFullName());
        existingUser.setEmail(userDetails.getEmail());
        existingUser.setPhone(userDetails.getPhone());
        existingUser.setRole(userDetails.getRole());
        existingUser.setUpdatedAt(LocalDateTime.now());
        
        User updatedUser = userRepository.save(existingUser);
        log.info("Updated user with id: {}", updatedUser.getId());
        return updatedUser;
    }

    public User deactivateUser(UUID userId) {
        User user = getUserById(userId);
        user.setIsActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        
        User deactivatedUser = userRepository.save(user);
        log.info("Deactivated user with id: {}", userId);
        return deactivatedUser;
    }

    public User activateUser(UUID userId) {
        User user = getUserById(userId);
        user.setIsActive(true);
        user.setUpdatedAt(LocalDateTime.now());
        
        User activatedUser = userRepository.save(user);
        log.info("Activated user with id: {}", userId);
        return activatedUser;
    }

    public void deleteUser(UUID userId) {
        User user = getUserById(userId);
        userRepository.delete(Objects.requireNonNull(user, "user must not be null"));
        log.info("Deleted user with id: {}", userId);
    }

    public List<User> searchUsers(String searchTerm) {
        return userRepository.findByNameOrEmailContaining(searchTerm);
    }

    public long getTotalUsersCount() {
        return userRepository.count();
    }

    public long getUsersByRoleCount(UserRole role) {
        return userRepository.countByRole(role);
    }

    public long getActiveUsersCount() {
        return userRepository.countByIsActive(true);
    }

    public long getInactiveUsersCount() {
        return userRepository.countByIsActive(false);
    }

    public User promoteToOrganizer(UUID userId) {
        User user = getUserById(userId);
        if (user.getRole() == UserRole.ORGANIZER) {
            throw new IllegalArgumentException("User is already an organizer");
        }
        user.setRole(UserRole.ORGANIZER);
        user.setUpdatedAt(LocalDateTime.now());
        User updated = userRepository.save(user);
        log.info("Promoted user {} to ORGANIZER", userId);
        return updated;
    }

    @SuppressWarnings("null")
    public CreateStaffAccountResponse createStaffAccountByOrganizer(User organizer, CreateStaffAccountRequest request) {
        if (organizer.getRole() != UserRole.ORGANIZER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only organizer can create staff accounts");
        }

        String normalizedEmail = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already registered");
        }

        User staff = User.builder()
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .phone(request.phone())
                .role(UserRole.STAFF)
            .organizerId(organizer.getId())
                .isActive(true)
                .isVerified(true)
                .build();

        User saved = Objects.requireNonNull(userRepository.save(staff), "saved staff must not be null");
        log.info("Organizer {} created staff account {}", organizer.getId(), saved.getId());

        return new CreateStaffAccountResponse(
                saved.getId(),
                saved.getEmail(),
                saved.getFullName(),
                saved.getRole(),
                Boolean.TRUE.equals(saved.getIsActive())
        );
    }

    public User demoteToCustomer(UUID userId) {
        User user = getUserById(userId);
        if (user.getRole() == UserRole.CUSTOMER) {
            throw new IllegalArgumentException("User is already a customer");
        }
        user.setRole(UserRole.CUSTOMER);
        user.setUpdatedAt(LocalDateTime.now());
        User updated = userRepository.save(user);
        log.info("Demoted user {} to CUSTOMER", userId);
        return updated;
    }
}
