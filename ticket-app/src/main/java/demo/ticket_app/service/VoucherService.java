package demo.ticket_app.service;

import demo.ticket_app.dto.voucher.CreateOrganizerVoucherRequest;
import demo.ticket_app.dto.voucher.CreateVoucherResponse;
import demo.ticket_app.entity.*;
import demo.ticket_app.exception.ResourceNotFoundException;
import demo.ticket_app.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
        
        if (!event.getOrganizerId().equals(organizerId)) {
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
}
