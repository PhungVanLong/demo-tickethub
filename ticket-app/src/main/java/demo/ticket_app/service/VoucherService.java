package demo.ticket_app.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import demo.ticket_app.dto.voucher.CreateOrganizerVoucherRequest;
import demo.ticket_app.dto.voucher.CreateVoucherResponse;
import demo.ticket_app.dto.voucher.ValidateVoucherResponse;
import demo.ticket_app.entity.DiscountType;
import demo.ticket_app.entity.Event;
import demo.ticket_app.entity.EventStatus;
import demo.ticket_app.entity.UserRole;
import demo.ticket_app.entity.Voucher;
import demo.ticket_app.entity.VoucherApplyOn;
import demo.ticket_app.entity.VoucherType;
import demo.ticket_app.exception.ResourceNotFoundException;
import demo.ticket_app.repository.EventRepository;
import demo.ticket_app.repository.MonthlyVoucherAllocationRepository;
import demo.ticket_app.repository.TicketTierRepository;
import demo.ticket_app.repository.UserRepository;
import demo.ticket_app.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class VoucherService {

    private final VoucherRepository voucherRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final MonthlyVoucherAllocationRepository monthlyAllocationRepository;
    private final TicketTierRepository ticketTierRepository;

    /**
     * Create a voucher for a specific event (organizer only)
     */
    public CreateVoucherResponse createEventVoucher(
            Long eventId,
            CreateOrganizerVoucherRequest request,
            UUID organizerId) {
        
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        
        if (!canManageEventVoucher(event, organizerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the organizer of this event");
        }
        
        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Event is not published yet");
        }
        
        Voucher voucher = Voucher.builder()
                .code(generateVoucherCode())
                .voucherType(VoucherType.ORGANIZER_EVENT)
                .discountType(request.discountType())
                .discountValue(request.discountValue())
                .minOrderValue(request.minOrderValue())
                .usageLimit(request.usageLimit())
                .usedCount(0)
                .validFrom(request.validFrom())
                .validUntil(request.validUntil())
                .applyOn(VoucherApplyOn.SPECIFIC_EVENT)
                .eventId(eventId)
                .organizerId(organizerId)
                .createdBy(organizerId)
                .isActive(true)
                .build();
        
        Voucher saved = voucherRepository.save(voucher);
        log.info("Organizer {} created event voucher {} for event {}", organizerId, saved.getCode(), eventId);
        
        return new CreateVoucherResponse(saved.getId(), saved.getCode(), saved.getValidFrom(), saved.getValidUntil());
    }

    /**
     * Create monthly vouchers for a user (system/admin only)
     */
    public void createMonthlyVoucher(UUID userId, String code, BigDecimal discountValue, LocalDateTime validUntil) {
        Voucher voucher = Voucher.builder()
                .code(code)
                .voucherType(VoucherType.USER_MONTHLY)
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(discountValue)
                .usageLimit(1)
                .usedCount(0)
                .validFrom(LocalDateTime.now())
                .validUntil(validUntil)
                .applyOn(VoucherApplyOn.ALL)
                .assignedToUser(userId)
                .createdBy(userId)  // System created
                .isActive(true)
                .build();
        
        voucherRepository.save(voucher);
        log.info("Created monthly voucher {} for user {}", code, userId);
    }

    /**
     * Get active vouchers for a user
     */
    public List<Voucher> getUserVouchers(UUID userId) {
        return voucherRepository.findValidUserVouchers(userId, LocalDateTime.now());
    }

    /**
     * Get organizer's event vouchers
     */
    public List<Voucher> getOrganizerEventVouchers(UUID organizerId) {
        return voucherRepository.findByOrganizerIdAndVoucherType(organizerId, VoucherType.ORGANIZER_EVENT);
    }

    /**
     * Validate a voucher for the current user and checkout context.
     */
    @Transactional(readOnly = true)
    public ValidateVoucherResponse validateVoucher(String code, Long eventId, BigDecimal orderAmount, UUID userId) {
        Optional<Voucher> voucherOptional = voucherRepository.findActiveVoucherByCode(code, LocalDateTime.now());
        if (voucherOptional.isEmpty()) {
            return new ValidateVoucherResponse(
                    false,
                    "Voucher is invalid or expired",
                    null,
                    code,
                    null,
                    null,
                    null,
                    BigDecimal.ZERO,
                    null,
                    null,
                    eventId,
                    null
            );
        }

        Voucher voucher = voucherOptional.get();

        if (voucher.getUsageLimit() != null && voucher.getUsedCount() != null && voucher.getUsedCount() >= voucher.getUsageLimit()) {
            return invalidVoucher(voucher, eventId, "Voucher usage limit reached");
        }

        if (voucher.getAssignedToUser() != null && !voucher.getAssignedToUser().equals(userId)) {
            return invalidVoucher(voucher, eventId, "Voucher does not belong to this user");
        }

        if (voucher.getApplyOn() == VoucherApplyOn.SPECIFIC_EVENT) {
            if (eventId == null) {
                return invalidVoucher(voucher, null, "Event id is required for this voucher");
            }
            if (voucher.getEventId() != null && !voucher.getEventId().equals(eventId)) {
                return invalidVoucher(voucher, eventId, "Voucher is not applicable for this event");
            }
        }

        if (voucher.getMinOrderValue() != null && orderAmount != null && orderAmount.compareTo(voucher.getMinOrderValue()) < 0) {
            return invalidVoucher(voucher, eventId, "Order does not meet voucher minimum value");
        }

        BigDecimal calculatedDiscount = BigDecimal.ZERO;
        if (orderAmount != null) {
            calculatedDiscount = calculateDiscountAmount(voucher, orderAmount);
        }

        return new ValidateVoucherResponse(
                true,
                "Voucher is valid",
                voucher.getId(),
                voucher.getCode(),
                voucher.getVoucherType(),
                voucher.getDiscountType(),
                voucher.getDiscountValue(),
                calculatedDiscount,
                voucher.getMinOrderValue(),
                voucher.getApplyOn(),
                voucher.getEventId(),
                voucher.getValidUntil()
        );
    }

    /**
     * Disable expired vouchers
     */
    @Transactional
    public long expireVouchers() {
        List<Voucher> expired = voucherRepository.findAll().stream()
                .filter(v -> v.getIsActive() && v.getValidUntil().isBefore(LocalDateTime.now()))
                .toList();
        
        expired.forEach(v -> {
            v.setIsActive(false);
            voucherRepository.save(v);
        });
        
        log.info("Expired {} vouchers", expired.size());
        return expired.size();
    }

    private String generateVoucherCode() {
        return "VOC-" + System.currentTimeMillis();
    }

    private boolean canManageEventVoucher(Event event, UUID userId) {
        if (event.getOrganizerId().equals(userId)) {
            return true;
        }

        return userRepository.findById(userId)
                .map(user -> user.getRole() == UserRole.ADMIN)
                .orElse(false);
    }

    private ValidateVoucherResponse invalidVoucher(Voucher voucher, Long eventId, String message) {
        return new ValidateVoucherResponse(
                false,
                message,
                voucher.getId(),
                voucher.getCode(),
                voucher.getVoucherType(),
                voucher.getDiscountType(),
                voucher.getDiscountValue(),
                BigDecimal.ZERO,
                voucher.getMinOrderValue(),
                voucher.getApplyOn(),
                voucher.getEventId() != null ? voucher.getEventId() : eventId,
                voucher.getValidUntil()
        );
    }

    private BigDecimal calculateDiscountAmount(Voucher voucher, BigDecimal orderAmount) {
        if (voucher.getDiscountType() == DiscountType.PERCENTAGE) {
            return orderAmount
                    .multiply(voucher.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        return voucher.getDiscountValue().min(orderAmount);
    }
}
