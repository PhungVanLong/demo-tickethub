package demo.ticket_app.dto.auth;

import java.util.UUID;

import demo.ticket_app.entity.UserRole;

public record CreateStaffAccountResponse(
        UUID userId,
        String email,
        String fullName,
        UserRole role,
        boolean active
) {
}
