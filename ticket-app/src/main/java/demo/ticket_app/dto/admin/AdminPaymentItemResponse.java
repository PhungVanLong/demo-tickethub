package demo.ticket_app.dto.admin;

import demo.ticket_app.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record AdminPaymentItemResponse(
        UUID paymentId,
        String paymentCode,
        UUID orderId,
        String paymentMethod,
        PaymentStatus paymentStatus,
        BigDecimal amount,
        BigDecimal platformFeeAmount,
        BigDecimal gatewayFeeAmount,
        BigDecimal organizerNetAmount,
        String transactionId,
        LocalDateTime paidAt,
        LocalDateTime createdAt
) {
}
