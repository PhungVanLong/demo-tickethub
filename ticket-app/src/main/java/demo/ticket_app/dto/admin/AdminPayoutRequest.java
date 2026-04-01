package demo.ticket_app.dto.admin;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AdminPayoutRequest(
        @NotNull LocalDateTime from,
        @NotNull LocalDateTime to,
        String payoutReference,
        String note
) {
}
