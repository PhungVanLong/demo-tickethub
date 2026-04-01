package demo.ticket_app.dto.checkout;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import demo.ticket_app.entity.OrderStatus;

public record CreateCheckoutOrderResponse(
        UUID orderId,
        String orderCode,
        OrderStatus status,
        BigDecimal amount,
        LocalDateTime holdExpiresAt
) {
}
