package ar.edu.utn.frc.tup.p4.services.game.state;

import ar.edu.utn.frc.tup.p4.entities.Game;
import ar.edu.utn.frc.tup.p4.entities.GameStatus;
import ar.edu.utn.frc.tup.p4.entities.Move;
import ar.edu.utn.frc.tup.p4.entities.Player;
import ar.edu.utn.frc.tup.p4.exceptions.InvalidGameStateException;
import ar.edu.utn.frc.tup.p4.services.game.DiceRoller;
import ar.edu.utn.frc.tup.p4.services.game.TurnEngine;
import ar.edu.utn.frc.tup.p4.services.game.validation.MoveValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * IN_PROGRESS: the only phase where rolling is allowed.
 * validate (Chain) → roll the die → play the turn → FINISHED or next turn.
 */
@Component
@RequiredArgsConstructor
public class InProgressState implements GameState {

    private final List<MoveValidator> moveValidators;
    private final DiceRoller diceRoller;
    private final TurnEngine turnEngine;

    @Override
    public GameStatus supports() {
        return GameStatus.IN_PROGRESS;
    }

    @Override
    public void start(Game game) {
        throw new InvalidGameStateException("Game " + game.getId() + " is already in progress");
    }

    @Override
    public Move roll(Game game, Player player) {
        for (MoveValidator validator : moveValidators) {
            validator.validate(game, player);
        }

        Move move = turnEngine.play(game, player, diceRoller.roll());

        Optional<Player> winner = turnEngine.findWinner(game, player);
        if (winner.isPresent()) {
            game.setWinner(winner.get());
            game.setStatus(GameStatus.FINISHED);
        } else {
            turnEngine.advanceTurn(game);
        }
        return move;
    }
}
