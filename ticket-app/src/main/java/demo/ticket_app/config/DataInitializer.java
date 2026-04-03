package demo.ticket_app.config;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import demo.ticket_app.entity.User;
import demo.ticket_app.entity.UserRole;
import demo.ticket_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.email:admin@tickethub.com}")
    private String adminEmail;

    @Value("${admin.password:Admin@123456}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        userRepository.findByEmail(adminEmail).ifPresentOrElse(existingAdmin -> {
            existingAdmin.setRole(UserRole.ADMIN);
            existingAdmin.setIsActive(true);
            existingAdmin.setIsVerified(true);
            existingAdmin.setPasswordHash(passwordEncoder.encode(adminPassword));
            if (existingAdmin.getFullName() == null || existingAdmin.getFullName().isBlank()) {
                existingAdmin.setFullName("System Administrator");
            }

            userRepository.save(Objects.requireNonNull(existingAdmin));
            log.info("Repaired admin account: {}", adminEmail);
            log.info("Admin password reset from config for local/dev usage");
        }, () -> {
            User admin = User.builder()
                    .email(adminEmail)
                    .passwordHash(passwordEncoder.encode(adminPassword))
                    .fullName("System Administrator")
                    .role(UserRole.ADMIN)
                    .isActive(true)
                    .isVerified(true)
                    .build();

            userRepository.save(Objects.requireNonNull(admin));
            log.info("Created admin account: {}", adminEmail);
            log.info("Admin password: {} (change after first login)", adminPassword);
        });
    }
}
