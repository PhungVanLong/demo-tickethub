package demo.ticket_app.service;

import demo.ticket_app.entity.OrderItem;
import demo.ticket_app.entity.Ticket;
import demo.ticket_app.entity.TicketStatus;
import demo.ticket_app.repository.OrderItemRepository;
import demo.ticket_app.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TicketService {

    private final TicketRepository ticketRepository;
    private final OrderItemRepository orderItemRepository;

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
        Ticket ticket = ticketRepository.findById(ticketId)
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
        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found"));
    }
}
