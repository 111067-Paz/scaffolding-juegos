package ar.edu.utn.frc.tup.p4.services.game.state;

import ar.edu.utn.frc.tup.p4.entities.Game;
import ar.edu.utn.frc.tup.p4.entities.GameStatus;
import ar.edu.utn.frc.tup.p4.entities.Move;
import ar.edu.utn.frc.tup.p4.entities.Player;
import ar.edu.utn.frc.tup.p4.exceptions.InvalidGameStateException;
import org.springframework.stereotype.Component;

/** WAITING: the game can be started; nobody can roll yet. */
@Component
public class WaitingState implements GameState {

    @Override
    public GameStatus supports() {
        return GameStatus.WAITING;
    }

    @Override
    public void start(Game game) {
        game.setCurrentTurn(0);
        game.setStatus(GameStatus.IN_PROGRESS);
    }

    @Override
    public Move roll(Game game, Player player) {
        throw new InvalidGameStateException("Game " + game.getId() + " has not started yet");
    }
}
