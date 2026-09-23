package ar.edu.utn.frc.tup.p4.exceptions;

import ar.edu.utn.frc.tup.p4.dtos.ErrorApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

/**
 * Single error contract: EVERY handler returns ErrorApi.
 * Status semantics: 404 = does not exist, 400 = invalid input/business rule,
 * 409 = state conflict, 500 = unexpected (generic handler ALWAYS last).
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    @ExceptionHandler({GameNotFoundException.class, PlayerNotFoundException.class})
    public ResponseEntity<ErrorApi> handleNotFound(RuntimeException exception) {
        log.warn("Resource not found: {}", exception.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler({InvalidGameStateException.class, InvalidMoveException.class})
    public ResponseEntity<ErrorApi> handleGameConflict(RuntimeException exception) {
        log.warn("Game rule violated: {}", exception.getMessage());
        return buildResponse(HttpStatus.CONFLICT, exception.getMessage());
    }

    // @Version detected a concurrent change (e.g. two rolls at the same time)
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorApi> handleOptimisticLock(ObjectOptimisticLockingFailureException exception) {
        log.warn("Concurrent modification: {}", exception.getMessage());
        return buildResponse(HttpStatus.CONFLICT,
                "The game was modified by another request. Reload and try again.");
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorApi> handleUserAlreadyExists(UserAlreadyExistsException exception) {
        log.warn("User already exists: {}", exception.getMessage());
        return buildResponse(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorApi> handleInvalidCredentials(InvalidCredentialsException exception) {
        log.warn("Failed login attempt");
        return buildResponse(HttpStatus.UNAUTHORIZED, exception.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorApi> handleUnauthorized(UnauthorizedException exception) {
        log.warn("Unauthorized request: {}", exception.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, exception.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorApi> handleIllegalArgument(IllegalArgumentException exception) {
        log.warn("Illegal argument: {}", exception.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorApi> handleValidation(MethodArgumentNotValidException exception) {
        String details = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("Validation failed: {}", details);
        return buildResponse(HttpStatus.BAD_REQUEST, details);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorApi> handleUnreadableBody(HttpMessageNotReadableException exception) {
        log.warn("Malformed request body: {}", exception.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "Malformed request body");
    }

    // Generic handler — ALWAYS last, never expose internals to the client
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorApi> handleGeneric(Exception exception) {
        log.error("Unexpected error", exception);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error. Please try again later.");
    }

    private ResponseEntity<ErrorApi> buildResponse(HttpStatus status, String message) {
        ErrorApi errorResponse = ErrorApi.builder()
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .build();
        return ResponseEntity.status(status).body(errorResponse);
    }
}
