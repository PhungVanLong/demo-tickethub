package demo.ticket_app.controller;

import demo.ticket_app.dto.admin.AdminPaymentItemResponse;
import demo.ticket_app.dto.admin.AdminPayoutExecuteResponse;
import demo.ticket_app.dto.admin.AdminPayoutPreviewResponse;
import demo.ticket_app.dto.admin.AdminPayoutRequest;
import demo.ticket_app.dto.admin.AdminRevenueSummaryResponse;
import demo.ticket_app.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AdminRevenueController {

    private final PaymentService paymentService;

    @GetMapping("/revenue/summary")
    public ResponseEntity<AdminRevenueSummaryResponse> getRevenueSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(paymentService.getRevenueSummary(from, to));
    }

    @GetMapping("/payments")
    public ResponseEntity<List<AdminPaymentItemResponse>> getPayments(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(paymentService.getPayments(from, to));
    }

    @GetMapping("/payouts/organizers/{organizerId}/preview")
    public ResponseEntity<AdminPayoutPreviewResponse> previewPayout(
            @PathVariable UUID organizerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(paymentService.getPayoutPreview(organizerId, from, to));
    }

    @PostMapping("/payouts/organizers/{organizerId}")
    public ResponseEntity<AdminPayoutExecuteResponse> executePayout(
            @PathVariable UUID organizerId,
            @Valid @RequestBody AdminPayoutRequest request) {
        return ResponseEntity.ok(paymentService.executePayout(organizerId, request));
    }
}
