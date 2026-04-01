package demo.ticket_app.dto.checkout;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CheckoutItemRequest(
        @NotNull Long ticketTierId,
        @NotNull @Min(1) Integer quantity
) {
}
