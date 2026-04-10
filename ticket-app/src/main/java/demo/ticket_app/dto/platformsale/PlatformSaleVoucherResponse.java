package demo.ticket_app.dto.platformsale;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PlatformSaleVoucherResponse(
        UUID id,
        String name,
        String description,
        BigDecimal discountPercentage,
        LocalDateTime validFrom,
        LocalDateTime validUntil,
        Boolean isActive,
        UUID voucherId,
        String voucherCode
) {}
