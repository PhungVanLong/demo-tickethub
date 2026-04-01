package demo.ticket_app.dto.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FakePaymentWebhookRequest(
        @NotBlank String paymentCode,
        @NotNull Boolean success,
        String transactionId,
        String message
) {
}
