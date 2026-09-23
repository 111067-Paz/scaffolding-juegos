package ar.edu.utn.frc.tup.p4.exceptions;

/** 409: the action is not allowed in the current game phase (State pattern). */
public class InvalidGameStateException extends RuntimeException {

    public InvalidGameStateException(String message) {
        super(message);
    }
}
