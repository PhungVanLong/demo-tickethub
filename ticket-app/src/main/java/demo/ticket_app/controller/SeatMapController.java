package demo.ticket_app.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import demo.ticket_app.config.SecurityUtils;
import demo.ticket_app.entity.SeatMap;
import demo.ticket_app.service.SeatMapService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;

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

    /**
     * Create a seat map by uploading an image (multipart/form-data).
     * Params:
     *   name  – display name of the seat map (required)
     *   file  – image file (optional if imageUrl is provided)
     *   imageUrl – pre-hosted image URL (optional if file is provided)
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SeatMap> create(
            @PathVariable Long eventId,
            @RequestParam("name") @NotBlank String name,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "imageUrl", required = false) String imageUrl) {
        UUID organizerId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(seatMapService.create(eventId, name, file, imageUrl, organizerId));
    }

    @PutMapping(value = "/{seatMapId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SeatMap> updateImage(
            @PathVariable Long eventId,
            @PathVariable Long seatMapId,
            @RequestParam("file") MultipartFile file) {
        UUID organizerId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(seatMapService.updateImage(eventId, seatMapId, file, organizerId));
    }

    @DeleteMapping("/{seatMapId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long eventId,
            @PathVariable Long seatMapId) {
        UUID organizerId = securityUtils.getCurrentUserId();
        seatMapService.delete(eventId, seatMapId, organizerId);
        return ResponseEntity.noContent().build();
    }
}
