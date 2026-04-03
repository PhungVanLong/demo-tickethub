package demo.ticket_app.dto.voucher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import demo.ticket_app.entity.DiscountType;
import demo.ticket_app.entity.VoucherApplyOn;
import demo.ticket_app.entity.VoucherType;

public record ValidateVoucherResponse(
        boolean valid,
        String message,
        UUID voucherId,
        String code,
        VoucherType voucherType,
        DiscountType discountType,
        BigDecimal discountValue,
        BigDecimal calculatedDiscount,
        BigDecimal minOrderValue,
        VoucherApplyOn applyOn,
        Long eventId,
        LocalDateTime validUntil
) {
}
