package demo.ticket_app.dto.checkout;

import java.math.BigDecimal;

public record CheckoutTierResponse(
        Long ticketTierId,
        String name,
        BigDecimal price,
        Integer quantityAvailable
) {
}
