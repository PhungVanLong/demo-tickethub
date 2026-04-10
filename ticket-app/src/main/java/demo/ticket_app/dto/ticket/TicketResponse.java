package demo.ticket_app.dto.ticket;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import demo.ticket_app.entity.TicketStatus;

/**
 * Response DTO for ticket with full context
 * Includes ticket info, buyer info, event info, and pricing
 */
public record TicketResponse(
        // Ticket Information
        UUID ticketId,
        String ticketCode,
        String qrCodeData,
        String seatNumber,
        TicketStatus ticketStatus,
        UUID orderItemId,
        LocalDateTime createdAt,
        LocalDateTime usedAt,
        
        // Buyer Information
        UUID buyerId,
        String buyerFullName,
        String buyerEmail,
        String buyerPhone,
        
        // Event Information
        Long eventId,
        String eventTitle,
        String eventVenue,
        String eventCity,
        LocalDateTime eventStartTime,
        String eventBannerUrl,
        
        // Tier Information
        Long tierId,
        String tierName,
        String tierType,
        BigDecimal unitPrice
) {
    /**
     * Convert Ticket entity to full DTO (basic info only, no enrichment)
     * Use enriched method from service for complete data
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
                ticket.getUsedAt(),
                null, null, null, null,  // buyer info - requires eager load
                null, null, null, null, null, null,  // event info - requires eager load
                null, null, null, null  // tier info - requires eager load
        );
    }
}
