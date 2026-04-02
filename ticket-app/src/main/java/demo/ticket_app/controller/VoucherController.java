package demo.ticket_app.controller;

import demo.ticket_app.config.SecurityUtils;
import demo.ticket_app.dto.voucher.CreateOrganizerVoucherRequest;
import demo.ticket_app.dto.voucher.CreateVoucherResponse;
import demo.ticket_app.entity.Voucher;
import demo.ticket_app.service.VoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class VoucherController {

    private final VoucherService voucherService;
    private final SecurityUtils securityUtils;

    /**
     * Get my available vouchers (user only)
     */
    @GetMapping("/me")
    public ResponseEntity<List<Voucher>> getMyVouchers() {
        UUID userId = securityUtils.getCurrentUserId();
        List<Voucher> vouchers = voucherService.getUserVouchers(userId);
        return ResponseEntity.ok(vouchers);
    }

    /**
     * Create a voucher for a specific event (organizer only)
     */
    @PostMapping("/events/{eventId}")
    public ResponseEntity<CreateVoucherResponse> createEventVoucher(
            @PathVariable Long eventId,
            @Valid @RequestBody CreateOrganizerVoucherRequest request) {
        UUID organizerId = securityUtils.getCurrentUserId();
        CreateVoucherResponse response = voucherService.createEventVoucher(eventId, request, organizerId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get organization's event vouchers (organizer only)
     */
    @GetMapping("/organizer")
    public ResponseEntity<List<Voucher>> getOrganizerVouchers() {
        UUID organizerId = securityUtils.getCurrentUserId();
        List<Voucher> vouchers = voucherService.getOrganizerEventVouchers(organizerId);
        return ResponseEntity.ok(vouchers);
    }
}
