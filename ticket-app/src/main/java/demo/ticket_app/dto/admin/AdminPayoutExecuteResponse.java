package demo.ticket_app.dto.admin;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record AdminPayoutExecuteResponse(
        UUID organizerId,
        long paidPayments,
        BigDecimal totalPaid,
        String payoutReference,
        LocalDateTime paidAt,
        String note
) {
}
