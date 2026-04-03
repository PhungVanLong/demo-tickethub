package demo.ticket_app.dto.ticket;

import demo.ticket_app.entity.TicketStatus;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for ticket
 * Contains QR code data string for FE to generate QR code
 */
public record TicketResponse(
        UUID ticketId,
        String ticketCode,
        String qrCodeData,
        String seatNumber,
        TicketStatus ticketStatus,
        UUID orderItemId,
        LocalDateTime createdAt,
        LocalDateTime usedAt
) {
    /**
     * Convert Ticket entity to DTO
     */
    public static TicketResponse from(demo.ticket_app.entity.Ticket ticket) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getTicketCode(),
                ticket.getQrCodeData(),
                ticket.getSeatNumber(),
                ticket.getTicketStatus(),
                ticket.getOrderItemId(),
                ticket.getCreatedAt(),
                ticket.getUsedAt()
        );
    }
}
