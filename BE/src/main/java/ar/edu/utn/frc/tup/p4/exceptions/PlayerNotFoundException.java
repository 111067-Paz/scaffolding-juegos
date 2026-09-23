package ar.edu.utn.frc.tup.p4.exceptions;

/** 404: the player id does not belong to the requested game. */
public class PlayerNotFoundException extends RuntimeException {

    public PlayerNotFoundException(Long playerId, Long gameId) {
        super("Player " + playerId + " not found in game " + gameId);
    }
}
