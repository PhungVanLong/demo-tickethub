package demo.ticket_app.dto.admin;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record AdminPayoutPreviewResponse(
        UUID organizerId,
        LocalDateTime from,
        LocalDateTime to,
        long pendingPayments,
        BigDecimal totalNetPayout
) {
}
