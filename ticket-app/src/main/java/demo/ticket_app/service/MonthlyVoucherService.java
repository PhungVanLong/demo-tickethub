package demo.ticket_app.service;

import demo.ticket_app.entity.MonthlyVoucherAllocation;
import demo.ticket_app.entity.User;
import demo.ticket_app.repository.MonthlyVoucherAllocationRepository;
import demo.ticket_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MonthlyVoucherService {

    private final MonthlyVoucherAllocationRepository allocationRepository;
    private final UserRepository userRepository;
    private final VoucherService voucherService;

    @Value("${voucher.monthly.quantity:2}")
    private Integer monthlyVoucherQuantity;

    @Value("${voucher.monthly.discount-percentage:10}")
    private BigDecimal monthlyVoucherDiscount;

    /**
     * Scheduled task: Create monthly vouchers for all users (runs on 1st of each month at 00:00)
     */
    @Scheduled(cron = "0 0 0 1 * *")  // First day of each month at midnight
    public void allocateMonthlyVouchersForAllUsers() {
        log.info("Starting monthly voucher allocation...");
        
        YearMonth currentMonth = YearMonth.now();
        String yearMonth = currentMonth.toString();  // Format: "2026-04"
        
        List<User> allUsers = userRepository.findAll();
        int createdCount = 0;
        
        for (User user : allUsers) {
            try {
                // Check if already allocated for this month
                var existing = allocationRepository.findByUserIdAndYearMonth(user.getId(), yearMonth);
                if (existing.isPresent()) {
                    log.debug("User {} already has allocation for {}", user.getId(), yearMonth);
                    continue;
                }
                
                // Create allocation record
                MonthlyVoucherAllocation allocation = MonthlyVoucherAllocation.builder()
                        .userId(user.getId())
                        .yearMonth(yearMonth)
                        .quantityAllocated(monthlyVoucherQuantity)
                        .quantityCreated(0)
                        .build();
                
                MonthlyVoucherAllocation saved = allocationRepository.save(allocation);
                
                // Create individual vouchers
                for (int i = 0; i < monthlyVoucherQuantity; i++) {
                    String code = "MONTHLY-" + yearMonth.replace("-", "") + "-" + user.getId() + "-" + i;
                    LocalDateTime validUntil = currentMonth.atEndOfMonth().atTime(23, 59, 59);
                    voucherService.createMonthlyVoucher(user.getId(), code, monthlyVoucherDiscount, validUntil);
                }
                
                // Update allocation count
                saved.setQuantityCreated(monthlyVoucherQuantity);
                allocationRepository.save(saved);
                createdCount++;
                
            } catch (Exception e) {
                log.error("Failed to allocate vouchers for user {}: {}", user.getId(), e.getMessage());
            }
        }
        
        log.info("Monthly voucher allocation completed. Created for {} users", createdCount);
    }

    /**
     * Scheduled task: Expire old vouchers (runs daily at 01:00)
     */
    @Scheduled(cron = "0 0 1 * * *")  // Every day at 1 AM
    public void expireOldVouchers() {
        log.info("Starting voucher expiration task...");
        long expiredCount = voucherService.expireVouchers();
        log.info("Expired {} vouchers", expiredCount);
    }

    /**
     * Get allocation info for a user in the current month
     */
    public MonthlyVoucherAllocation getCurrentMonthAllocation(UUID userId) {
        String currentMonth = YearMonth.now().toString();
        return allocationRepository.findByUserIdAndYearMonth(userId, currentMonth)
                .orElse(null);
    }

    /**
     * Get allocation info for a specific month
     */
    public MonthlyVoucherAllocation getAllocationForMonth(UUID userId, String yearMonth) {
        return allocationRepository.findByUserIdAndYearMonth(userId, yearMonth)
                .orElse(null);
    }
}
