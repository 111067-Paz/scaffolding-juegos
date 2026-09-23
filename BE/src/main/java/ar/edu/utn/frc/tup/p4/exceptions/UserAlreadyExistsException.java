package ar.edu.utn.frc.tup.p4.exceptions;

/**
 * Conflicto de estado (username o email ya registrado)
 * → mapeado a 409 CONFLICT en GlobalExceptionHandler.
 */
public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String field, String value) {
        super("A user with " + field + " '" + value + "' already exists");
    }
}
