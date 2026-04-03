package demo.ticket_app.dto.event;

import java.util.UUID;

public record OrganizerSummaryResponse(
        UUID id,
        String name,
        boolean verified
) {
}
