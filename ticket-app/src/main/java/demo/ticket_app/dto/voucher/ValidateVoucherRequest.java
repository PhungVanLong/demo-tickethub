package demo.ticket_app.dto.voucher;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

public record ValidateVoucherRequest(
        @NotBlank String code,
        Long eventId,
        @DecimalMin(value = "0.0", inclusive = true) BigDecimal orderAmount
) {
}
