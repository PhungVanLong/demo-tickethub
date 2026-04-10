package demo.ticket_app.dto.seatmap;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record ReleaseSeatHoldRequest(
        @NotNull UUID holdToken
) {
}
