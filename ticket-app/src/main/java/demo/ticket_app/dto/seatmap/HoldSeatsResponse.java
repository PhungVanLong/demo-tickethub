package demo.ticket_app.dto.seatmap;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record HoldSeatsResponse(
        UUID holdToken,
        LocalDateTime expiresAt,
        List<SeatItemResponse> seats
) {
}
