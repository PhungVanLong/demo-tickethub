package demo.ticket_app.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import demo.ticket_app.dto.ticket.TicketResponse;
import demo.ticket_app.entity.OrderItem;
import demo.ticket_app.entity.Ticket;
import demo.ticket_app.entity.TicketStatus;
import demo.ticket_app.entity.TicketTier;
import demo.ticket_app.repository.EventRepository;
import demo.ticket_app.repository.OrderItemRepository;
import demo.ticket_app.repository.SeatMapRepository;
import demo.ticket_app.repository.TicketRepository;
import demo.ticket_app.repository.TicketTierRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TicketService {

    private final TicketRepository ticketRepository;
    private final OrderItemRepository orderItemRepository;
    private final TicketTierRepository ticketTierRepository;
    private final SeatMapRepository seatMapRepository;
    private final EventRepository eventRepository;

    /**
     * Create tickets for a specific order (called after successful payment)
     */
    public List<Ticket> createTicketsForOrder(UUID orderId) {
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);

        if (orderItems.isEmpty()) {
            log.warn("No order items found for order {}", orderId);
            return new ArrayList<>();
        }

        List<Ticket> ticketsToCreate = new ArrayList<>();

        for (OrderItem item : orderItems) {
            // Create N tickets based on quantity
            for (int i = 0; i < item.getQuantity(); i++) {
                Ticket ticket = Ticket.builder()
                        .orderItemId(item.getId())
                        .ticketStatus(TicketStatus.ACTIVE)
                        .build();
                ticketsToCreate.add(ticket);
            }
        }

        // Generate QR code data for each ticket after they get IDs
        List<Ticket> savedTickets = ticketRepository.saveAll(ticketsToCreate);
        
        // Update QR code data with ticket info
        savedTickets.forEach(ticket -> {
            ticket.setQrCodeData(generateQrCodeData(ticket));
        });
        
        List<Ticket> ticketsWithQr = ticketRepository.saveAll(savedTickets);
        log.info("Created {} tickets for order {}", ticketsWithQr.size(), orderId);
        return ticketsWithQr;
    }
    
    /**
     * Generate QR code data string for ticket
     * FE will use this string to generate actual QR code
     */
    private String generateQrCodeData(Ticket ticket) {
        // Format: TICKET|{ticketCode}|{ticketId}|{orderItemId}|{status}
        return String.format("TICKET|%s|%s|%s|%s", 
                ticket.getTicketCode(),
                ticket.getId(),
                ticket.getOrderItemId(),
                ticket.getTicketStatus());
    }

    /**
     * Get tickets by order ID
     */
    public List<Ticket> getTicketsByOrderId(UUID orderId) {
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        List<Ticket> allTickets = new ArrayList<>();

        for (OrderItem item : items) {
            allTickets.addAll(item.getTickets() != null ? item.getTickets() : new ArrayList<>());
        }

        return allTickets;
    }

    /**
     * Get tickets by user account.
     */
    @Transactional(readOnly = true)
    public List<Ticket> getTicketsByUserId(UUID userId) {
        return ticketRepository.findByUserId(userId);
    }

    /**
     * Mark ticket as used
     */
    public Ticket useTicket(UUID ticketId) {
        UUID safeTicketId = Objects.requireNonNull(ticketId, "ticketId must not be null");
        Ticket ticket = ticketRepository.findById(safeTicketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));

        if (ticket.getTicketStatus() != TicketStatus.ACTIVE) {
            throw new IllegalArgumentException("Ticket is not active");
        }

        ticket.setTicketStatus(TicketStatus.USED);
        ticket.setUsedAt(java.time.LocalDateTime.now());

        return ticketRepository.save(ticket);
    }

    /**
     * Get ticket by ID
     */
    public Ticket getTicketById(UUID ticketId) {
        UUID safeTicketId = Objects.requireNonNull(ticketId, "ticketId must not be null");
        return ticketRepository.findById(safeTicketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));
    }

    /**
     * Convert Ticket entity to enriched TicketResponse DTO with full context
     * Includes buyer info, event info, and tier info by eager loading relationships
     */
    @Transactional(readOnly = true)
    public TicketResponse toEnrichedResponse(Ticket ticket) {
        // Eagerly load related data
        UUID safeOrderItemId = Objects.requireNonNull(ticket.getOrderItemId(), "orderItemId must not be null");
        OrderItem orderItem = ticket.getOrderItem() != null ? ticket.getOrderItem() : 
            orderItemRepository.findById(safeOrderItemId).orElse(null);

        if (orderItem == null) {
            // Return response with minimal data if orderItem not found
            return new TicketResponse(
                    ticket.getId(),
                    ticket.getTicketCode(),
                    ticket.getQrCodeData(),
                    ticket.getSeatNumber(),
                    ticket.getTicketStatus(),
                    ticket.getOrderItemId(),
                    ticket.getCreatedAt(),
                    ticket.getUsedAt(),
                    null, null, null, null,
                    null, null, null, null, null, null,
                    null, null, null, null
            );
        }

        // Get buyer info from Order → User
        UUID buyerId = null;
        String buyerFullName = null;
        String buyerEmail = null;
        String buyerPhone = null;

        if (orderItem.getOrder() != null && orderItem.getOrder().getUser() != null) {
            var user = orderItem.getOrder().getUser();
            buyerId = user.getId();
            buyerFullName = user.getFullName();
            buyerEmail = user.getEmail();
            buyerPhone = user.getPhone();
        }

        // Resolve tier from relation or direct lookup.
        TicketTier tier = orderItem.getTicketTier();
        Long tierId = orderItem.getTicketTierId();
        if (tier == null && tierId != null) {
            tier = ticketTierRepository.findById(tierId).orElse(null);
        }

        // Get event info from TicketTier -> SeatMap -> Event
        Long eventId = null;
        String eventTitle = null;
        String eventVenue = null;
        String eventCity = null;
        java.time.LocalDateTime eventStartTime = null;
        String eventBannerUrl = null;

        if (tier != null && tier.getSeatMapId() != null) {
            Long seatMapId = Objects.requireNonNull(tier.getSeatMapId(), "seatMapId must not be null");
            var seatMap = tier.getSeatMap() != null
                    ? tier.getSeatMap()
                : seatMapRepository.findById(seatMapId).orElse(null);

            if (seatMap != null) {
            Long resolvedEventId = Objects.requireNonNull(seatMap.getEventId(), "eventId must not be null");
                var event = seatMap.getEvent() != null
                        ? seatMap.getEvent()
                : eventRepository.findById(resolvedEventId).orElse(null);

                if (event != null) {
                    eventId = event.getId();
                    eventTitle = event.getTitle();
                    eventVenue = event.getVenue();
                    eventCity = event.getCity();
                    eventStartTime = event.getStartTime();
                    eventBannerUrl = event.getBannerUrl();
                }
            }
        }

        return new TicketResponse(
                ticket.getId(),
                ticket.getTicketCode(),
                ticket.getQrCodeData(),
                ticket.getSeatNumber(),
                ticket.getTicketStatus(),
                ticket.getOrderItemId(),
                ticket.getCreatedAt(),
                ticket.getUsedAt(),
                // Buyer info
                buyerId,
                buyerFullName,
                buyerEmail,
                buyerPhone,
                // Event info
                eventId,
                eventTitle,
                eventVenue,
                eventCity,
                eventStartTime,
                eventBannerUrl,
                // Tier info
                tier != null ? tier.getId() : null,
                tier != null ? tier.getName() : null,
                tier != null && tier.getTierType() != null ? tier.getTierType().toString() : null,
                orderItem.getUnitPrice()
        );
    }
}
