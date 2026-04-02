package demo.ticket_app.dto.event;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateEventRequest(
        @NotBlank @Size(max = 255) String title,
        @Size(max = 100) String category,
        String description,
        @NotBlank @Size(max = 255) String venue,
        @NotBlank @Size(max = 100) String city,
        @Size(max = 100) String country,
        @Size(max = 100) String locationCoords,
        @NotNull LocalDateTime startTime,
        @NotNull LocalDateTime endTime,
        @Size(max = 500) String bannerUrl,
        @Size(max = 500) String imageUrl,
        Boolean featured,
        List<String> tags
) {
}
