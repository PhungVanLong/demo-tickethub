package demo.ticket_app.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import demo.ticket_app.service.PlatformSaleService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/platform-sales")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class PublicPlatformSaleController {
    private final PlatformSaleService platformSaleService;

    /**
     * Lấy tất cả voucher thuộc các platform sale đang active (public, FE dùng để hiển thị trong My Voucher)
     */
    @GetMapping("/active-vouchers")
    public ResponseEntity<List<demo.ticket_app.dto.voucher.VoucherResponse>> getAllVouchersInActivePlatformSales() {
        var vouchers = platformSaleService.getAllVouchersInActivePlatformSales()
                .stream()
                .map(demo.ticket_app.dto.voucher.VoucherResponse::from)
                .toList();
        return ResponseEntity.ok(vouchers);
    }

    /**
     * Get platform sale info by voucher code (public, FE dùng để kiểm tra trạng thái PlatformSale của voucher platform)
     */
    @GetMapping("/voucher/{voucherCode}")
    public ResponseEntity<?> getPlatformSaleByVoucherCode(@PathVariable String voucherCode) {
        var sale = platformSaleService.findByVoucherCode(voucherCode);
        return ResponseEntity.ok(new demo.ticket_app.dto.platformsale.PlatformSaleVoucherResponse(
                sale.getId(),
                sale.getName(),
                sale.getDescription(),
                sale.getDiscountPercentage(),
                sale.getValidFrom(),
                sale.getValidUntil(),
                sale.getIsActive(),
                sale.getVoucher().getId(),
                sale.getVoucher().getCode()
        ));
    }
}