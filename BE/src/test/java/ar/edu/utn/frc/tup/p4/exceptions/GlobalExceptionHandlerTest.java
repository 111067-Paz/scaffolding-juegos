package ar.edu.utn.frc.tup.p4.exceptions;

import ar.edu.utn.frc.tup.p4.dtos.ErrorApi;
import ar.edu.utn.frc.tup.p4.entities.Game;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Branches not reachable from the controller slices (concurrency, unexpected errors). */
@Tag("unit")
@DisplayName("GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("optimistic lock failure → 409 with a reload hint")
    void handleOptimisticLock_returns409() {
        ResponseEntity<ErrorApi> response = handler.handleOptimisticLock(
                new ObjectOptimisticLockingFailureException(Game.class, 10L));

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(409, response.getBody().getStatus());
    }

    @Test
    @DisplayName("IllegalArgumentException → 400")
    void handleIllegalArgument_returns400() {
        ResponseEntity<ErrorApi> response = handler.handleIllegalArgument(new IllegalArgumentException("bad"));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("bad", response.getBody().getMessage());
    }

    @Test
    @DisplayName("unexpected exception → 500 without leaking internals")
    void handleGeneric_returns500WithGenericMessage() {
        ResponseEntity<ErrorApi> response = handler.handleGeneric(new IllegalStateException("db password=123"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Internal server error. Please try again later.", response.getBody().getMessage());
    }
}
