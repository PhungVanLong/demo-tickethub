package demo.ticket_app.dto.tickettier;

import demo.ticket_app.entity.TicketTierType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateTicketTierRequest(
        @NotBlank @Size(max = 100) String name,
        @NotNull TicketTierType tierType,
        @NotNull @DecimalMin("0") BigDecimal price,
        @NotNull @Min(1) Integer quantityTotal,
        @Size(max = 10) String colorCode,
        LocalDateTime saleStart,
        LocalDateTime saleEnd
) {
}
