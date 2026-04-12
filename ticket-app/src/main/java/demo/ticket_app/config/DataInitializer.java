package demo.ticket_app.config;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import demo.ticket_app.entity.Order;
import demo.ticket_app.entity.OrderStatus;
import demo.ticket_app.entity.User;
import demo.ticket_app.entity.UserRole;
import demo.ticket_app.repository.OrderRepository;
import demo.ticket_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final OrderRepository orderRepository;

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
        // Seed Order đơn giản, mỗi Order liên kết với user ngẫu nhiên
        long orderCount = orderRepository.count();
        int orderTarget = 1_000_000;
        if (orderCount < orderTarget) {
            log.info("Seeding thêm {} order đơn giản...", orderTarget - orderCount);
            java.util.List<User> allUsers = userRepository.findAll();
            if (allUsers.isEmpty()) {
                log.warn("Không có user nào để seed order!");
                return;
            }
            java.util.List<Order> orders = new java.util.ArrayList<>();
            java.util.Random rand = new java.util.Random();
            for (int i = (int)orderCount; i < orderTarget; i++) {
                User user = allUsers.get(rand.nextInt(allUsers.size()));
                String orderCode = "ORD" + String.format("%06d", i);
                java.math.BigDecimal total = java.math.BigDecimal.valueOf(100_000 + rand.nextInt(900_000));
                Order order = Order.builder()
                        .id(java.util.UUID.randomUUID())
                        .userId(user.getId())
                        .orderCode(orderCode)
                        .orderStatus(OrderStatus.CONFIRMED)
                        .totalAmount(total)
                        .discountAmount(java.math.BigDecimal.valueOf(0))
                        .finalAmount(total)
                        .notes("Seed order " + i)
                        .build();
                orders.add(order);
                if (orders.size() == 1000) {
                    orderRepository.saveAll(orders);
                    orders.clear();
                    log.info("Seeded {} orders...", i + 1);
                }
            }
            if (!orders.isEmpty()) {
                orderRepository.saveAll(orders);
            }
            log.info("Seeding orders completed!");
        } else {
            log.info("Order table already có {} orders, skip seeding.", orderCount);
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

        // Seed user không dùng Faker, chỉ dùng dữ liệu ngẫu nhiên đơn giản
        long userCount = userRepository.count();
        int target = 100_000;
        if (userCount < target) {
            log.info("Seeding thêm {} user đơn giản...", target - userCount);
            java.util.List<User> users = new java.util.ArrayList<>();
            for (int i = (int)userCount; i < target; i++) {
                String email = "user" + i + "@example.com";
                String fullName = "User " + i;
                String phone = "09" + String.format("%08d", i);
                String avatarUrl = "https://api.adorable.io/avatars/285/" + email;
                User user = User.builder()
                        .id(java.util.UUID.randomUUID())
                        .email(email)
                        .passwordHash(passwordEncoder.encode("Password@123"))
                        .fullName(fullName)
                        .role(UserRole.CUSTOMER)
                        .isActive(true)
                        .isVerified(i % 2 == 0)
                        .phone(phone)
                        .avatarUrl(avatarUrl)
                        .build();
                users.add(user);
                // Batch insert mỗi 1000 bản ghi
                if (users.size() == 1000) {
                    userRepository.saveAll(users);
                    users.clear();
                    log.info("Seeded {} users...", i + 1);
                }
            }
            if (!users.isEmpty()) {
                userRepository.saveAll(users);
            }
            log.info("Seeding users completed!");
        } else {
            log.info("User table already có {} users, skip seeding.", userCount);
        }
    }
}
