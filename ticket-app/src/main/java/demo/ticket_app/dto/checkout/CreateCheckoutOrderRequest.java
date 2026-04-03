package demo.ticket_app.dto.checkout;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CreateCheckoutOrderRequest(
        @NotNull Long eventId,
        @NotEmpty List<@Valid CheckoutItemRequest> items,
        String voucherCode
) {
}
