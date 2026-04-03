package demo.ticket_app.dto.payment;

import demo.ticket_app.entity.OrderStatus;
import demo.ticket_app.entity.PaymentStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record FakePaymentWebhookResponse(
        UUID orderId,
        String paymentCode,
        PaymentStatus paymentStatus,
        OrderStatus orderStatus,
        BigDecimal amount,
        BigDecimal platformFeeAmount,
        BigDecimal gatewayFeeAmount,
        BigDecimal organizerNetAmount,
        String message
) {
}
