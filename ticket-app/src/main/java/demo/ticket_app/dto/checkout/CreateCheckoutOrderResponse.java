package demo.ticket_app.dto.checkout;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import demo.ticket_app.entity.OrderStatus;

public record CreateCheckoutOrderResponse(
        UUID id,
        String orderCode,
        OrderStatus status,
        BigDecimal totalAmount,
        LocalDateTime createdAt
) {
}
