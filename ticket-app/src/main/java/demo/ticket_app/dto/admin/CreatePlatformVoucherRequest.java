package demo.ticket_app.dto.admin;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import demo.ticket_app.entity.DiscountType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreatePlatformVoucherRequest(
        @NotBlank @Size(max = 255) String name,
        @NotNull DiscountType discountType,
        @NotNull @Positive BigDecimal discountValue,
        @Positive BigDecimal minOrderValue,
        @Positive Integer usageLimit,
        @NotNull @FutureOrPresent LocalDateTime validFrom,
        @NotNull @Future LocalDateTime validUntil
) {
}
