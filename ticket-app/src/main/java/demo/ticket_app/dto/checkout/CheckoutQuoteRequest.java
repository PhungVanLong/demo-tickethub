package demo.ticket_app.dto.checkout;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CheckoutQuoteRequest(
        @NotNull UUID userId,
        @NotNull Long eventId,
        @NotEmpty List<@Valid CheckoutItemRequest> items,
        String voucherCode
) {
}
