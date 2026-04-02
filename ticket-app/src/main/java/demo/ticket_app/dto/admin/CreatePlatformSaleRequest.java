package demo.ticket_app.dto.admin;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreatePlatformSaleRequest(
        @NotBlank @Size(max = 255) String name,
        @Size(max = 500) String description,
        @NotNull @Positive @DecimalMax("100.00") BigDecimal discountPercentage,
        @NotNull @FutureOrPresent LocalDateTime validFrom,
        @NotNull @Future LocalDateTime validUntil
) {}
