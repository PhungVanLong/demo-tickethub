package demo.ticket_app.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import demo.ticket_app.entity.PlatformVoucher;
import demo.ticket_app.service.PlatformVoucherService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/platform-vouchers")
@RequiredArgsConstructor
public class PlatformVoucherController {
    private final PlatformVoucherService platformVoucherService;

    @PostMapping
    public ResponseEntity<PlatformVoucher> create(@RequestBody PlatformVoucher voucher) {
        PlatformVoucher saved = platformVoucherService.create(voucher);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{code}")
    public ResponseEntity<PlatformVoucher> getByCode(@PathVariable String code) {
        return platformVoucherService.findByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
