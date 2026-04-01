package demo.ticket_app.dto.payment;

import jakarta.validation.constraints.NotBlank;

public record CreatePaymentIntentRequest(
        @NotBlank String method,
        String returnUrl,
        String cancelUrl
) {
}
