package demo.ticket_app.dto.seatmap;

import java.time.LocalDateTime;

import demo.ticket_app.entity.SeatStatus;

public record SeatItemResponse(
        Long seatId,
        String seatCode,
        String rowLabel,
        Integer colNumber,
        Long ticketTierId,
        SeatStatus status,
        LocalDateTime holdExpiresAt
) {
}
