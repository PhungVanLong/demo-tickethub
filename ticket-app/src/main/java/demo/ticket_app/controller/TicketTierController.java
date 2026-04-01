package demo.ticket_app.controller;

import demo.ticket_app.config.SecurityUtils;
import demo.ticket_app.dto.tickettier.CreateTicketTierRequest;
import demo.ticket_app.entity.TicketTier;
import demo.ticket_app.service.TicketTierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/events/{eventId}/seat-maps/{seatMapId}/tiers")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class TicketTierController {

    private final TicketTierService ticketTierService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public ResponseEntity<List<TicketTier>> getByEventAndSeatMap(
            @PathVariable Long eventId,
            @PathVariable Long seatMapId) {
        return ResponseEntity.ok(ticketTierService.getByEventAndSeatMap(eventId, seatMapId));
    }

    @PostMapping
    public ResponseEntity<TicketTier> create(
            @PathVariable Long eventId,
            @PathVariable Long seatMapId,
            @Valid @RequestBody CreateTicketTierRequest request) {
        UUID organizerId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(ticketTierService.create(eventId, seatMapId, request, organizerId));
    }

    @DeleteMapping("/{tierId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long eventId,
            @PathVariable Long seatMapId,
            @PathVariable Long tierId) {
        UUID organizerId = securityUtils.getCurrentUserId();
        ticketTierService.delete(eventId, seatMapId, tierId, organizerId);
        return ResponseEntity.noContent().build();
    }
}
