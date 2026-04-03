package demo.ticket_app.dto.voucher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import demo.ticket_app.entity.DiscountType;
import demo.ticket_app.entity.Voucher;
import demo.ticket_app.entity.VoucherApplyOn;
import demo.ticket_app.entity.VoucherType;

public record VoucherResponse(
        UUID id,
        String code,
        VoucherType voucherType,
        DiscountType discountType,
        BigDecimal discountValue,
        BigDecimal minOrderValue,
        Integer usageLimit,
        Integer usedCount,
        LocalDateTime validFrom,
        LocalDateTime validUntil,
        VoucherApplyOn applyOn,
        Long eventId,
        UUID organizerId,
        UUID assignedToUser,
        UUID createdBy,
        Boolean isActive
) {
    public static VoucherResponse from(Voucher voucher) {
        return new VoucherResponse(
                voucher.getId(),
                voucher.getCode(),
                voucher.getVoucherType(),
                voucher.getDiscountType(),
                voucher.getDiscountValue(),
                voucher.getMinOrderValue(),
                voucher.getUsageLimit(),
                voucher.getUsedCount(),
                voucher.getValidFrom(),
                voucher.getValidUntil(),
                voucher.getApplyOn(),
                voucher.getEventId(),
                voucher.getOrganizerId(),
                voucher.getAssignedToUser(),
                voucher.getCreatedBy(),
                voucher.getIsActive()
        );
    }
}
