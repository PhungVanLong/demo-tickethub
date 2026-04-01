package demo.ticket_app.dto.auth;

import demo.ticket_app.entity.UserRole;

public record AuthResponse(
        String accessToken,
        String tokenType,
        java.util.UUID userId,
        String email,
        String fullName,
        UserRole role
) {
}
