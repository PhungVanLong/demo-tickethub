package demo.ticket_app.controller;

import demo.ticket_app.config.SecurityUtils;
import demo.ticket_app.dto.seatmap.CreateSeatMapRequest;
import demo.ticket_app.entity.SeatMap;
import demo.ticket_app.service.SeatMapService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/events/{eventId}/seat-maps")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class SeatMapController {

    private final SeatMapService seatMapService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public ResponseEntity<List<SeatMap>> getByEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(seatMapService.getByEvent(eventId));
    }

    @PostMapping
    public ResponseEntity<SeatMap> create(
            @PathVariable Long eventId,
            @Valid @RequestBody CreateSeatMapRequest request) {
        UUID organizerId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(seatMapService.create(eventId, request, organizerId));
    }

    @DeleteMapping("/{seatMapId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long eventId,
            @PathVariable Long seatMapId) {
        UUID organizerId = securityUtils.getCurrentUserId();
        seatMapService.delete(eventId, seatMapId, organizerId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/upload-image")
    public ResponseEntity<Map<String, String>> uploadSeatMapImage(
            @PathVariable Long eventId,
            @RequestParam("file") MultipartFile file) {
        UUID organizerId = securityUtils.getCurrentUserId();
        String imageUrl = seatMapService.uploadSeatMapImage(eventId, organizerId, file);
        return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
    }
}
