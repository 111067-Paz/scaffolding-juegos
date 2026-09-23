package ar.edu.utn.frc.tup.p4.exceptions;

/** 409: a move validator (Chain of Responsibility) rejected the move. */
public class InvalidMoveException extends RuntimeException {

    public InvalidMoveException(String message) {
        super(message);
    }
}
