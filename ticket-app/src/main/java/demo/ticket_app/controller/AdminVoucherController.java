package demo.ticket_app.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import demo.ticket_app.config.SecurityUtils;
import demo.ticket_app.dto.admin.CreatePlatformVoucherRequest;
import demo.ticket_app.dto.voucher.CreateVoucherResponse;
import demo.ticket_app.service.VoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/vouchers")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AdminVoucherController {

    private final VoucherService voucherService;
    private final SecurityUtils securityUtils;

    @PostMapping("/platform")
    public ResponseEntity<CreateVoucherResponse> createPlatformVoucher(
            @Valid @RequestBody CreatePlatformVoucherRequest request) {
        var adminId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(voucherService.createPlatformVoucher(request, adminId));
    }
}
