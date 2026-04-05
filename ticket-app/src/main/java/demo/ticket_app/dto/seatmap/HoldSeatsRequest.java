package demo.ticket_app.dto.seatmap;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record HoldSeatsRequest(
        @NotEmpty List<@NotNull Long> seatIds
) {
}
