package demo.ticket_app.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import demo.ticket_app.entity.Event;
import demo.ticket_app.entity.SeatMap;
import demo.ticket_app.exception.ResourceNotFoundException;
import demo.ticket_app.repository.EventRepository;
import demo.ticket_app.repository.SeatMapRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class SeatMapService {

    private static final Path SEAT_MAP_UPLOAD_DIR = Paths.get("uploads", "seatmaps");
    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = Arrays.asList(
            ".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp", ".svg"
    );

    private final SeatMapRepository seatMapRepository;
    private final EventRepository eventRepository;

    @Transactional(readOnly = true)
    public List<SeatMap> getByEvent(Long eventId) {
        validateEventExists(eventId);
        return seatMapRepository.findByEventId(eventId);
    }

    /**
     * Create an image-based seat map.
     * Accepts either a file upload or a pre-hosted imageUrl (or both; file takes precedence).
     */
    public SeatMap create(Long eventId, String name, MultipartFile file, String imageUrl, UUID organizerId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));
        if (!event.getOrganizerId().equals(organizerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the organizer of this event");
        }
        String resolvedImageUrl = resolveImageUrl(file, imageUrl);

        SeatMap seatMap = SeatMap.builder()
                .eventId(eventId)
                .name(name)
                .imageUrl(resolvedImageUrl)
                .totalRows(0)
                .totalCols(0)
                .build();

        return seatMapRepository.save(seatMap);
    }

    /**
     * Replace the image of an existing seat map.
     */
    public SeatMap updateImage(Long eventId, Long seatMapId, MultipartFile file, UUID organizerId) {
        return updateImage(eventId, seatMapId, file, null, organizerId);
    }

    public SeatMap updateImage(Long eventId, Long seatMapId, MultipartFile file, String imageUrl, UUID organizerId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));
        if (!event.getOrganizerId().equals(organizerId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the organizer of this event");
        }
        SeatMap seatMap = seatMapRepository.findById(seatMapId)
                .orElseThrow(() -> new ResourceNotFoundException("Seat map not found with id: " + seatMapId));

        seatMap.setImageUrl(resolveImageUrl(file, imageUrl));
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

    private String storeImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image file is required");
        }

        String originalName = Objects.requireNonNullElse(file.getOriginalFilename(), "");
        String contentType = file.getContentType();
        boolean imageContentType = contentType != null && contentType.toLowerCase(Locale.ROOT).startsWith("image/");
        boolean imageExtension = hasAllowedImageExtension(originalName);
        if (!imageContentType && !imageExtension) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only image files are allowed");
        }

        try {
            Files.createDirectories(SEAT_MAP_UPLOAD_DIR);
            if (originalName.isBlank()) {
                originalName = "seat-map";
            }
            String storedName = UUID.randomUUID() + extractExtension(originalName);
            Path destination = SEAT_MAP_UPLOAD_DIR.resolve(storedName);
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/seatmaps/" + storedName;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Cannot store seat map image");
        }
    }

    private String resolveImageUrl(MultipartFile file, String imageUrl) {
        boolean hasFile = file != null && !file.isEmpty();
        boolean hasImageUrl = imageUrl != null && !imageUrl.isBlank();

        if (!hasFile && !hasImageUrl) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Either an image file or an imageUrl must be provided");
        }

        if (hasFile) {
            return storeImage(file);
        }

        String normalized = imageUrl.trim();
        if (!isValidImageUrl(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "imageUrl must start with http://, https://, or /uploads/");
        }
        return normalized;
    }

    private boolean isValidImageUrl(String imageUrl) {
        return imageUrl.startsWith("http://")
                || imageUrl.startsWith("https://")
                || imageUrl.startsWith("/uploads/");
    }

    private boolean hasAllowedImageExtension(String fileName) {
        String lower = fileName.toLowerCase(Locale.ROOT);
        return ALLOWED_IMAGE_EXTENSIONS.stream().anyMatch(lower::endsWith);
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
