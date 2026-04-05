package demo.ticket_app.dto.seatmap;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record ConfirmSeatHoldRequest(
        @NotNull UUID holdToken,
        UUID orderId
) {
}
