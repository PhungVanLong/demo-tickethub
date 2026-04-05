package demo.ticket_app.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import demo.ticket_app.config.SecurityUtils;
import demo.ticket_app.dto.seatmap.ConfirmSeatHoldRequest;
import demo.ticket_app.dto.seatmap.HoldSeatsRequest;
import demo.ticket_app.dto.seatmap.HoldSeatsResponse;
import demo.ticket_app.dto.seatmap.ReleaseSeatHoldRequest;
import demo.ticket_app.dto.seatmap.SeatItemResponse;
import demo.ticket_app.service.SeatSelectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/events/{eventId}/seat-maps/{seatMapId}/seats")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class SeatSelectionController {

    private final SeatSelectionService seatSelectionService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public ResponseEntity<List<SeatItemResponse>> getSeats(
            @PathVariable Long eventId,
            @PathVariable Long seatMapId) {
        return ResponseEntity.ok(seatSelectionService.getSeats(eventId, seatMapId));
    }

    @PostMapping("/hold")
    public ResponseEntity<HoldSeatsResponse> holdSeats(
            @PathVariable Long eventId,
            @PathVariable Long seatMapId,
            @Valid @RequestBody HoldSeatsRequest request) {
        UUID userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(seatSelectionService.holdSeats(eventId, seatMapId, request.seatIds(), userId));
    }

    @PostMapping("/release")
    public ResponseEntity<Void> releaseHold(
            @PathVariable Long eventId,
            @PathVariable Long seatMapId,
            @Valid @RequestBody ReleaseSeatHoldRequest request) {
        UUID userId = securityUtils.getCurrentUserId();
        seatSelectionService.releaseHold(eventId, seatMapId, request.holdToken(), userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/confirm")
    public ResponseEntity<Void> confirmHold(
            @PathVariable Long eventId,
            @PathVariable Long seatMapId,
            @Valid @RequestBody ConfirmSeatHoldRequest request) {
        UUID userId = securityUtils.getCurrentUserId();
        seatSelectionService.confirmHold(eventId, seatMapId, request.holdToken(), userId, request.orderId());
        return ResponseEntity.noContent().build();
    }
}
