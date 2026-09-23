package ar.edu.utn.frc.tup.p4.exceptions;

/**
 * 404. Also thrown when the game exists but belongs to another user:
 * answering 404 (not 403) does not reveal that the id exists.
 */
public class GameNotFoundException extends RuntimeException {

    public GameNotFoundException(Long gameId) {
        super("Game " + gameId + " not found");
    }
}
