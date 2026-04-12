package demo.ticket_app.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
    @Email @NotBlank String email,
    @NotBlank String otp,
    @NotBlank @Size(min = 6, max = 100) String newPassword
) {}
