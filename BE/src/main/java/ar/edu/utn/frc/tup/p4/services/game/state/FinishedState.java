package ar.edu.utn.frc.tup.p4.services.game.state;

import ar.edu.utn.frc.tup.p4.entities.Game;
import ar.edu.utn.frc.tup.p4.entities.GameStatus;
import ar.edu.utn.frc.tup.p4.entities.Move;
import ar.edu.utn.frc.tup.p4.entities.Player;
import ar.edu.utn.frc.tup.p4.exceptions.InvalidGameStateException;
import org.springframework.stereotype.Component;

/** FINISHED: terminal phase, every action is rejected. */
@Component
public class FinishedState implements GameState {

    @Override
    public GameStatus supports() {
        return GameStatus.FINISHED;
    }

    @Override
    public void start(Game game) {
        throw new InvalidGameStateException("Game " + game.getId() + " is already finished");
    }

    @Override
    public Move roll(Game game, Player player) {
        throw new InvalidGameStateException("Game " + game.getId() + " is already finished");
    }
}
