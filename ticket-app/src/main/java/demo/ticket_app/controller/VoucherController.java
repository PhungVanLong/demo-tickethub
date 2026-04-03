package demo.ticket_app.controller;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import demo.ticket_app.config.SecurityUtils;
import demo.ticket_app.dto.voucher.CreateOrganizerVoucherRequest;
import demo.ticket_app.dto.voucher.CreateVoucherResponse;
import demo.ticket_app.dto.voucher.ValidateVoucherRequest;
import demo.ticket_app.dto.voucher.ValidateVoucherResponse;
import demo.ticket_app.dto.voucher.VoucherResponse;
import demo.ticket_app.service.VoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

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
    public ResponseEntity<List<VoucherResponse>> getMyVouchers() {
        UUID userId = securityUtils.getCurrentUserId();
        List<VoucherResponse> vouchers = voucherService.getUserVouchers(userId).stream()
                .map(VoucherResponse::from)
                .collect(Collectors.toList());
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
     * Validate voucher code for current user during checkout.
     */
    @PostMapping("/validate")
    public ResponseEntity<ValidateVoucherResponse> validateVoucher(
            @Valid @RequestBody ValidateVoucherRequest request) {
        UUID userId = securityUtils.getCurrentUserId();
        ValidateVoucherResponse response = voucherService.validateVoucher(
                request.code(),
                request.eventId(),
                request.orderAmount(),
                userId
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Get organization's event vouchers (organizer only)
     */
    @GetMapping("/organizer")
    public ResponseEntity<List<VoucherResponse>> getOrganizerVouchers() {
        UUID organizerId = securityUtils.getCurrentUserId();
        List<VoucherResponse> vouchers = voucherService.getOrganizerEventVouchers(organizerId).stream()
                .map(VoucherResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(vouchers);
    }
}
