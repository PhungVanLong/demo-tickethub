package demo.ticket_app.dto.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CreateEventRequest(
        @NotBlank @Size(max = 255) String title,
        String description,
        @NotBlank @Size(max = 255) String venue,
        @NotBlank @Size(max = 100) String city,
        @Size(max = 100) String locationCoords,
        @NotNull LocalDateTime startTime,
        @NotNull LocalDateTime endTime,
        @Size(max = 500) String bannerUrl
) {
}
