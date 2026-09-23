package ar.edu.utn.frc.tup.p4.exceptions;

/**
 * Login fallido → 401 UNAUTHORIZED.
 * Mensaje genérico a propósito: no revelamos si falló el usuario o la contraseña.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Invalid username or password");
    }
}
