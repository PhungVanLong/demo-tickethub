package demo.ticket_app.dto.voucher;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateVoucherResponse(
        UUID voucherId,
        String code,
        LocalDateTime validFrom,
        LocalDateTime validUntil
) {}
