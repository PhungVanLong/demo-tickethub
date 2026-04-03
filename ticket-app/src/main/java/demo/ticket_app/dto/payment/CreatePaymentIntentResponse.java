package demo.ticket_app.dto.payment;

import java.math.BigDecimal;
import java.util.UUID;

import demo.ticket_app.entity.PaymentStatus;

public record CreatePaymentIntentResponse(
        UUID paymentId,
        String paymentCode,
        PaymentStatus status,
        BigDecimal amount,
        BigDecimal platformFeeAmount,
        BigDecimal gatewayFeeAmount,
        BigDecimal organizerNetAmount,
        String payUrl,
        java.time.LocalDateTime expiresAt
) {
}
