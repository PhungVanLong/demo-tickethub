package demo.ticket_app.dto.voucher;

import demo.ticket_app.entity.DiscountType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateOrganizerVoucherRequest(
        @NotBlank @Size(max = 255) String name,
        @NotNull DiscountType discountType,
        @NotNull @Positive BigDecimal discountValue,
        @Positive BigDecimal minOrderValue,
        @Positive Integer usageLimit,
        @NotNull @FutureOrPresent LocalDateTime validFrom,
        @NotNull @Future LocalDateTime validUntil
) {}
