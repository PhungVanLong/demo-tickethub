package demo.ticket_app.service;

import demo.ticket_app.dto.seatmap.CreateSeatMapRequest;
import demo.ticket_app.entity.Event;
import demo.ticket_app.entity.SeatMap;
import demo.ticket_app.exception.ResourceNotFoundException;
import demo.ticket_app.repository.EventRepository;
import demo.ticket_app.repository.SeatMapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class SeatMapService {

    private static final Path SEAT_MAP_UPLOAD_DIR = Paths.get("uploads", "seatmaps");

    private final SeatMapRepository seatMapRepository;
    private final EventRepository eventRepository;

    @Transactional(readOnly = true)
    public List<SeatMap> getByEvent(Long eventId) {
        validateEventExists(eventId);
        return seatMapRepository.findByEventId(eventId);
    }

    public SeatMap create(Long eventId, CreateSeatMapRequest request, UUID organizerId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));
        if (!event.getOrganizerId().equals(organizerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the organizer of this event");
        }

        SeatMap seatMap = SeatMap.builder()
                .eventId(eventId)
                .name(request.name())
                .layoutJson(request.layoutJson())
            .imageUrl(request.imageUrl())
                .totalRows(request.totalRows())
                .totalCols(request.totalCols())
                .build();

        return seatMapRepository.save(seatMap);
    }

    public void delete(Long eventId, Long seatMapId, UUID organizerId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));
        if (!event.getOrganizerId().equals(organizerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the organizer of this event");
        }
        SeatMap seatMap = seatMapRepository.findById(seatMapId)
                .orElseThrow(() -> new ResourceNotFoundException("Seat map not found with id: " + seatMapId));
        seatMapRepository.delete(seatMap);
    }

    public String uploadSeatMapImage(Long eventId, UUID organizerId, MultipartFile file) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));
        if (!event.getOrganizerId().equals(organizerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the organizer of this event");
        }

        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image file is required");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only image files are allowed");
        }

        try {
            Files.createDirectories(SEAT_MAP_UPLOAD_DIR);
            String originalName = Objects.requireNonNullElse(file.getOriginalFilename(), "seat-map");
            String extension = extractExtension(originalName);
            String storedName = UUID.randomUUID() + extension;
            Path destination = SEAT_MAP_UPLOAD_DIR.resolve(storedName);
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/seatmaps/" + storedName;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Cannot store seat map image");
        }
    }

    private String extractExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 0) {
            return "";
        }
        return fileName.substring(dot);
    }

    private void validateEventExists(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException("Event not found with id: " + eventId);
        }
    }
}
