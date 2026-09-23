package ar.edu.utn.frc.tup.p4.exceptions;

/**
 * Thrown when a protected endpoint is hit without a valid token.
 * Translated to 401 by the GlobalExceptionHandler.
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
