package demo.ticket_app.dto.admin;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminRevenueSummaryResponse(
        LocalDateTime from,
        LocalDateTime to,
        long successfulPayments,
        BigDecimal gmv,
        BigDecimal platformFee,
        BigDecimal gatewayFee,
        BigDecimal organizerNet
) {
}
