package demo.ticket_app.dto.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import demo.ticket_app.entity.Order;
import demo.ticket_app.entity.OrderStatus;

public record OrderResponse(
        UUID id,
        UUID userId,
        String orderCode,
        OrderStatus orderStatus,
        BigDecimal totalAmount,
        BigDecimal discountAmount,
        BigDecimal finalAmount,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getOrderCode(),
                order.getOrderStatus(),
                order.getTotalAmount(),
                order.getDiscountAmount(),
                order.getFinalAmount(),
                order.getNotes(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}