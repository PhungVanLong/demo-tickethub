package demo.ticket_app.dto.auth;

public record GoogleAuthPayload(
        String email,
        String fullName,
        String avatarUrl,
        boolean emailVerified
) {
}
