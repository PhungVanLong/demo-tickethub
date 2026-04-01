package demo.ticket_app.dto.seatmap;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateSeatMapRequest(
        @NotBlank @Size(max = 255) String name,
        String layoutJson,
        @Size(max = 500) String imageUrl,
        @NotNull @Min(1) Integer totalRows,
        @NotNull @Min(1) Integer totalCols
) {
}
