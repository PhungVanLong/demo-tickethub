package demo.ticket_app.exception;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import demo.ticket_app.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
        public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException ex, HttpServletRequest request) {
        int status = ex.getStatusCode().value();
                String code = switch (status) {
                        case 400 -> "VALIDATION_ERROR";
                        case 401 -> "UNAUTHORIZED";
                        case 403 -> "FORBIDDEN";
                        case 404 -> "NOT_FOUND";
                        default -> "APPLICATION_ERROR";
                };
        return ResponseEntity
                .status(status)
                                .body(ErrorResponse.of(status, code, ex.getReason(), List.of(), request.getRequestURI()));
    }

    @ExceptionHandler(AuthenticationException.class)
        public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex, HttpServletRequest request) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                                .body(ErrorResponse.of(401, "UNAUTHORIZED", "Invalid email or password", List.of(), request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
                List<ErrorResponse.FieldErrorItem> errors = ex.getBindingResult().getFieldErrors().stream()
                                .map(fieldError -> new ErrorResponse.FieldErrorItem(fieldError.getField(), fieldError.getDefaultMessage()))
                                .collect(Collectors.toList());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                                .body(ErrorResponse.of(400, "VALIDATION_ERROR", "Validation failed", errors, request.getRequestURI()));
    }

        @ExceptionHandler(HttpMessageNotReadableException.class)
                public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                                                .body(ErrorResponse.of(400, "VALIDATION_ERROR", "Invalid request payload", List.of(), request.getRequestURI()));
        }

    @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                                .body(ErrorResponse.of(400, "VALIDATION_ERROR", ex.getMessage(), List.of(), request.getRequestURI()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                                .body(ErrorResponse.of(404, "NOT_FOUND", ex.getMessage(), List.of(), request.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ErrorResponse.of(500, "INTERNAL_ERROR", "An unexpected error occurred", List.of(), request.getRequestURI()));
    }
}
