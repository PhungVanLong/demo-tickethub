package demo.ticket_app.controller;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import demo.ticket_app.config.SecurityUtils;
import demo.ticket_app.dto.ticket.TicketResponse;
import demo.ticket_app.entity.Ticket;
import demo.ticket_app.service.TicketService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class TicketController {

    private final TicketService ticketService;
    private final SecurityUtils securityUtils;

    /**
     * Get my tickets (user's confirmed order tickets)
     */
    @GetMapping("/me")
    public ResponseEntity<List<TicketResponse>> getMyTickets() {
        UUID userId = securityUtils.getCurrentUserId();
        List<TicketResponse> responses = ticketService.getTicketsByUserId(userId).stream()
                .map(ticketService::toEnrichedResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * Get tickets for a specific order
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<TicketResponse>> getTicketsByOrder(@PathVariable UUID orderId) {
        List<Ticket> tickets = ticketService.getTicketsByOrderId(orderId);
        List<TicketResponse> responses = tickets.stream()
                .map(ticketService::toEnrichedResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * Get ticket detail (includes qrCodeData for FE to generate QR)
     */
    @GetMapping("/{ticketId}")
    public ResponseEntity<TicketResponse> getTicketDetail(@PathVariable UUID ticketId) {
        Ticket ticket = ticketService.getTicketById(ticketId);
        return ResponseEntity.ok(ticketService.toEnrichedResponse(ticket));
    }

    /**
     * Mark ticket as used
     */
    @PostMapping("/{ticketId}/use")
    public ResponseEntity<TicketResponse> useTicket(@PathVariable UUID ticketId) {
        Ticket ticket = ticketService.useTicket(ticketId, securityUtils.getCurrentUser());
        return ResponseEntity.ok(ticketService.toEnrichedResponse(ticket));
    }

    /**
     * Download ticket (returns ticket info for FE to render)
     * FE will use qrCodeData to generate QR code
     */
    @GetMapping("/{ticketId}/download")
    public ResponseEntity<TicketResponse> downloadTicket(@PathVariable UUID ticketId) {
        Ticket ticket = ticketService.getTicketById(ticketId);
        return ResponseEntity.ok(ticketService.toEnrichedResponse(ticket));
    }
}
