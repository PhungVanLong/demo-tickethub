package demo.ticket_app.dto.seatmap;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Used only when creating a seat map with a pre-existing image URL (no file upload).
 * Primary flow uses multipart/form-data via controller params.
 */
public record CreateSeatMapRequest(
        @NotBlank @Size(max = 255) String name,
        @Size(max = 500) String imageUrl
) {
}
