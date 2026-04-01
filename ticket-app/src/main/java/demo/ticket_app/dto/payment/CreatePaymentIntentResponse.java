package demo.ticket_app.dto.payment;

import demo.ticket_app.entity.PaymentStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record CreatePaymentIntentResponse(
        UUID paymentId,
        String paymentCode,
        PaymentStatus status,
        BigDecimal amount,
        BigDecimal platformFeeAmount,
        BigDecimal gatewayFeeAmount,
        BigDecimal organizerNetAmount,
        String paymentUrl
) {
}
