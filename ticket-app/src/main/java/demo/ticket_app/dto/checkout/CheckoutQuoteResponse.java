package demo.ticket_app.dto.checkout;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CheckoutQuoteResponse(
        BigDecimal subtotal,
        BigDecimal discount,
        BigDecimal total,
        LocalDateTime expiresAt
) {
}
