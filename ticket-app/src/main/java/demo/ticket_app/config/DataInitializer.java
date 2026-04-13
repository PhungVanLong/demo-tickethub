package demo.ticket_app.config;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import demo.ticket_app.entity.Event;
import demo.ticket_app.entity.EventApproval;
import demo.ticket_app.entity.EventStatus;
import demo.ticket_app.entity.ApprovalDecision;
import demo.ticket_app.entity.SeatMap;
import demo.ticket_app.entity.TicketTier;
import demo.ticket_app.entity.TicketTierType;
import demo.ticket_app.entity.User;
import demo.ticket_app.entity.UserRole;
import demo.ticket_app.repository.EventApprovalRepository;
import demo.ticket_app.repository.EventRepository;
import demo.ticket_app.repository.SeatMapRepository;
import demo.ticket_app.repository.TicketTierRepository;
import demo.ticket_app.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final SeatMapRepository seatMapRepository;
    private final TicketTierRepository ticketTierRepository;
    private final EventApprovalRepository eventApprovalRepository;
    private final PasswordEncoder passwordEncoder;

    @PersistenceContext
    private final EntityManager entityManager;

    @Value("${admin.email:admin@tickethub.com}")
    private String adminEmail;

    @Value("${admin.password:Admin@123456}")
    private String adminPassword;

    // ──────────────── Cấu hình số lượng seed ────────────────
    private static final int USER_TARGET = 1000; // số user seed
    private static final int EVENT_TARGET = 5000; // số event seed
    private static final int CONTENTION_TEST_TARGET = 10;
    private static final int BATCH_SIZE = 50;

    // ──────────────── Dữ liệu mẫu ────────────────
    private static final String[] CATEGORIES = {
            "Âm nhạc", "Thể thao", "Công nghệ", "Nghệ thuật", "Ẩm thực",
            "Giáo dục", "Kinh doanh", "Giải trí", "Sức khỏe", "Du lịch"
    };
    private static final String[] CITIES = {
            "Hà Nội", "Hồ Chí Minh", "Đà Nẵng", "Cần Thơ", "Hải Phòng",
            "Huế", "Nha Trang", "Vũng Tàu", "Đà Lạt", "Quảng Ninh"
    };
    private static final String[] VENUES = {
            "Nhà hát Lớn", "Sân vận động Mỹ Đình", "Trung tâm Hội nghị Quốc gia",
            "Nhà thi đấu Phú Thọ", "Cung Văn hóa Hữu nghị", "Trung tâm văn hóa",
            "Sân khấu ngoài trời", "Khu triển lãm", "Trung tâm thương mại", "Rạp chiếu phim"
    };
    private static final String[] TIER_NAMES = { "VIP", "Standard", "Economy", "Student", "Group" };
    private static final String[] TIER_COLORS = { "#FFD700", "#4CAF50", "#2196F3", "#FF5722", "#9C27B0" };
    private static final String[] TAGS_POOL = {
            "hot", "trending", "newbie", "family", "outdoor", "indoor",
            "live", "free-parking", "vip-lounge", "limited"
    };

    private final Random random = new Random();

    @Override
    public void run(String... args) {
        ensureAdminAccount();
        seedUsers();
        seedEvents();
        seedContentionTestEvents();
    }

    // ════════════════════════════════════════════════════════
    // ADMIN
    // ════════════════════════════════════════════════════════
    private void ensureAdminAccount() {
        userRepository.findByEmail(adminEmail).ifPresentOrElse(admin -> {
            admin.setRole(UserRole.ADMIN);
            admin.setIsActive(true);
            admin.setIsVerified(true);
            admin.setPasswordHash(passwordEncoder.encode(adminPassword));
            if (admin.getFullName() == null || admin.getFullName().isBlank()) {
                admin.setFullName("System Administrator");
            }
            userRepository.save(Objects.requireNonNull(admin));
            log.info("Repaired admin account: {}", adminEmail);
        }, () -> {
            User admin = User.builder()
                    .email(adminEmail)
                    .passwordHash(passwordEncoder.encode(adminPassword))
                    .fullName("System Administrator")
                    .role(UserRole.ADMIN)
                    .isActive(true)
                    .isVerified(true)
                    .build();
            userRepository.save(admin);
            log.info("Created admin account: {} / password: {}", adminEmail, adminPassword);
        });
    }

    // ════════════════════════════════════════════════════════
    // USERS
    // ════════════════════════════════════════════════════════
    private void seedUsers() {
        long existing = userRepository.count();
        if (existing >= USER_TARGET) {
            log.info("Users: {} records exist, skip seeding.", existing);
            return;
        }
        log.info("Seeding {} users...", USER_TARGET - existing);
        List<User> batch = new ArrayList<>();
        for (int i = (int) existing; i < USER_TARGET; i++) {
            boolean isOrganizer = (i % 10 == 1); // 10% là organizer
            batch.add(User.builder()
                    .id(UUID.randomUUID())
                    .email("user" + i + "@tickethub.vn")
                    .passwordHash(passwordEncoder.encode("Password@123"))
                    .fullName(randomFullName(i))
                    .phone("09" + String.format("%08d", i))
                    .role(isOrganizer ? UserRole.ORGANIZER : UserRole.CUSTOMER)
                    .isActive(true)
                    .isVerified(i % 3 != 0)
                    .avatarUrl("https://i.pravatar.cc/150?u=user" + i)
                    .build());
            if (batch.size() == BATCH_SIZE) {
                userRepository.saveAll(batch);
                entityManager.flush();
                entityManager.clear();
                batch.clear();
            }
        }
        if (!batch.isEmpty()) {
            userRepository.saveAll(batch);
            entityManager.flush();
            entityManager.clear();
        }
        log.info("Users seeded: {} total", userRepository.count());
    }

    // ════════════════════════════════════════════════════════
    // EVENTS + SEAT MAPS + TICKET TIERS
    // ════════════════════════════════════════════════════════
    private void seedEvents() {
        long existing = eventRepository.count();
        if (existing >= EVENT_TARGET) {
            log.info("Events: {} records exist, skip seeding.", existing);
            return;
        }

        // Lấy danh sách organizer để gán cho event
        List<User> organizers = userRepository.findAll().stream()
                .filter(u -> u.getRole() == UserRole.ORGANIZER || u.getRole() == UserRole.ADMIN)
                .toList();
        if (organizers.isEmpty()) {
            log.warn("No organizer found, skip seeding events.");
            return;
        }

        // Lấy admin để approve event
        User admin = userRepository.findByEmail(adminEmail).orElse(organizers.get(0));

        log.info("Seeding {} events...", EVENT_TARGET - existing);
        for (int i = (int) existing; i < EVENT_TARGET; i++) {
            User organizer = organizers.get(i % organizers.size());
            String category = CATEGORIES[i % CATEGORIES.length];
            String city = CITIES[i % CITIES.length];
            String venue = VENUES[i % VENUES.length];

            LocalDateTime start = LocalDateTime.now().plusDays(random.nextInt(180) + 1);
            LocalDateTime end = start.plusHours(2 + random.nextInt(6));

            boolean published = (i % 5 != 0); // 80% published, 20% pending
            EventStatus status = published ? EventStatus.PUBLISHED : EventStatus.PENDING;

            String slug = "event-" + i + "-" + System.currentTimeMillis();
            String tags = randomTags();

            Event event = Event.builder()
                    .organizerId(organizer.getId())
                    .title(category + " " + city + " #" + (i + 1))
                    .slug(slug)
                    .category(category)
                    .description("Sự kiện " + category + " tại " + venue + ", " + city
                            + ". Đây là chương trình đặc sắc với nhiều hoạt động hấp dẫn dành cho mọi đối tượng. "
                            + "Hãy nhanh tay đặt vé để không bỏ lỡ cơ hội tuyệt vời này!")
                    .venue(venue)
                    .city(city)
                    .country("Vietnam")
                    .locationCoords("10." + random.nextInt(9999) + ",106." + random.nextInt(9999))
                    .startTime(start)
                    .endTime(end)
                    .bannerUrl("https://picsum.photos/seed/event" + i + "/800/300")
                    .imageUrl("https://picsum.photos/seed/thumb" + i + "/400/250")
                    .featured(i % 7 == 0)
                    .tags(tags)
                    .rating(BigDecimal.valueOf(3.0 + random.nextDouble() * 2.0).setScale(1,
                            java.math.RoundingMode.HALF_UP))
                    .reviewCount((long) random.nextInt(500))
                    .status(status)
                    .isPublished(published)
                    .build();

            Event savedEvent = eventRepository.save(event);

            // Tạo EventApproval nếu published
            if (published) {
                eventApprovalRepository.save(EventApproval.builder()
                        .eventId(savedEvent.getId())
                        .adminId(admin.getId())
                        .decision(ApprovalDecision.APPROVED)
                        .reason("Auto-approved by data seeder")
                        .decidedAt(LocalDateTime.now())
                        .build());
            }

            // Tạo SeatMap cho event
            SeatMap seatMap = seatMapRepository.save(SeatMap.builder()
                    .eventId(savedEvent.getId())
                    .name("Sơ đồ chỗ ngồi - " + venue)
                    .totalRows(10)
                    .totalCols(20)
                    .imageUrl("https://picsum.photos/seed/seatmap" + i + "/800/500")
                    .build());

            // Tạo 2–3 TicketTier cho mỗi SeatMap
            int tierCount = 2 + random.nextInt(2); // 2 hoặc 3 tier
            for (int t = 0; t < tierCount; t++) {
                int qty = 50 + random.nextInt(451); // 50–500 vé
                BigDecimal basePrice = BigDecimal.valueOf(100_000L * (t + 1) + random.nextInt(9) * 50_000L);
                ticketTierRepository.save(TicketTier.builder()
                        .seatMapId(seatMap.getId())
                        .name(TIER_NAMES[t % TIER_NAMES.length])
                        .tierType(t == 0 ? TicketTierType.VIP : TicketTierType.GENERAL)
                        .price(basePrice)
                        .quantityTotal(qty)
                        .quantityAvailable(qty - random.nextInt(qty / 2))
                        .colorCode(TIER_COLORS[t % TIER_COLORS.length])
                        .saleStart(start.minusDays(30))
                        .saleEnd(start.minusHours(1))
                        .build());
            }

            if ((i + 1) % BATCH_SIZE == 0) {
                entityManager.flush();
                entityManager.clear();
                log.info("Seeded {}/{} events...", i + 1, EVENT_TARGET);
                
                // Re-fetch organizers and admin since clear() detached them
                organizers = userRepository.findAll().stream()
                        .filter(u -> u.getRole() == UserRole.ORGANIZER || u.getRole() == UserRole.ADMIN)
                        .toList();
                admin = userRepository.findByEmail(adminEmail).orElse(organizers.get(0));
            }
        }
        log.info("Events seeded: {} total", eventRepository.count());
    }

    private void seedContentionTestEvents() {
        String titlePrefix = "Contention Test Event";
        // Optimized: Count directly in DB instead of loading all events
        long existing = eventRepository.countByTitleStartingWith(titlePrefix);

        if (existing >= CONTENTION_TEST_TARGET) {
            log.info("Contention Test Events: {} records exist, skip seeding.", existing);
            return;
        }

        List<User> organizers = userRepository.findAll().stream()
                .filter(u -> u.getRole() == UserRole.ORGANIZER || u.getRole() == UserRole.ADMIN)
                .toList();
        if (organizers.isEmpty())
            return;

        User admin = userRepository.findByEmail(adminEmail).orElse(organizers.get(0));
        User organizer = organizers.get(0);

        log.info("Seeding {} contention test events...", CONTENTION_TEST_TARGET - existing);
        for (int i = (int) existing; i < CONTENTION_TEST_TARGET; i++) {
            int qty = (i % 2 == 0) ? 1 : 2; // Alternating 1 and 2 tickets
            String title = titlePrefix + " #" + (i + 1);
            String slug = "contention-test-" + i + "-" + System.currentTimeMillis();

            Event event = Event.builder()
                    .organizerId(organizer.getId())
                    .title(title)
                    .slug(slug)
                    .category("Testing")
                    .description("Special event for concurrency testing. High contention, very few tickets available.")
                    .venue("Contention Lab")
                    .city("Hanoi")
                    .country("Vietnam")
                    .startTime(LocalDateTime.now().plusDays(30))
                    .endTime(LocalDateTime.now().plusDays(31))
                    .status(EventStatus.PUBLISHED)
                    .isPublished(true)
                    .featured(false)
                    .rating(BigDecimal.ZERO)
                    .reviewCount(0L)
                    .build();

            Event savedEvent = eventRepository.save(event);

            eventApprovalRepository.save(EventApproval.builder()
                    .eventId(savedEvent.getId())
                    .adminId(admin.getId())
                    .decision(ApprovalDecision.APPROVED)
                    .reason("Auto-approved contention test event")
                    .decidedAt(LocalDateTime.now())
                    .build());

            SeatMap seatMap = seatMapRepository.save(SeatMap.builder()
                    .eventId(savedEvent.getId())
                    .name("Contention Section")
                    .totalRows(1)
                    .totalCols(10)
                    .build());

            ticketTierRepository.save(TicketTier.builder()
                    .seatMapId(seatMap.getId())
                    .name("Limited Tier")
                    .tierType(TicketTierType.GENERAL)
                    .price(BigDecimal.valueOf(100_000))
                    .quantityTotal(qty + 5)
                    .quantityAvailable(qty)
                    .saleStart(LocalDateTime.now().minusDays(1))
                    .saleEnd(LocalDateTime.now().plusDays(30))
                    .build());
            
            if ((i + 1) % BATCH_SIZE == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
        log.info("Contention test events seeded: {} total", CONTENTION_TEST_TARGET);
    }

    // ════════════════════════════════════════════════════════
    // HELPERS
    // ════════════════════════════════════════════════════════
    private String randomFullName(int i) {
        String[] firstNames = { "Nguyễn", "Trần", "Lê", "Phạm", "Hoàng", "Vũ", "Đặng", "Bùi", "Đỗ", "Hồ" };
        String[] lastNames = { "Văn An", "Thị Bình", "Minh Châu", "Quốc Dũng", "Hữu Em",
                "Thành Phong", "Thị Lan", "Đức Long", "Văn Nam", "Thị Oanh" };
        return firstNames[i % firstNames.length] + " " + lastNames[i % lastNames.length];
    }

    private String randomTags() {
        List<String> selected = new ArrayList<>();
        for (int k = 0; k < 3; k++) {
            selected.add(TAGS_POOL[random.nextInt(TAGS_POOL.length)]);
        }
        return String.join(",", selected);
    }
}
