package demo.ticket_app.dto;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

public record ErrorResponse(
        String timestamp,
        int status,
        String code,
        String message,
        List<FieldErrorItem> errors,
        String path
) {
    public static ErrorResponse of(int status, String code, String message, List<FieldErrorItem> errors, String path) {
        return new ErrorResponse(Instant.now().atOffset(ZoneOffset.UTC).toString(), status, code, message, errors, path);
    }

    public record FieldErrorItem(String field, String message) {
    }
}
