package ar.edu.utn.frc.tup.p4.services.game;

import ar.edu.utn.frc.tup.p4.entities.CellType;
import ar.edu.utn.frc.tup.p4.entities.Game;
import ar.edu.utn.frc.tup.p4.entities.Move;
import ar.edu.utn.frc.tup.p4.entities.Player;
import ar.edu.utn.frc.tup.p4.services.game.effects.CellEffectRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * The rules of one turn, in strict order (traditional code: ordered steps with
 * side effects are clearer as statements than as a stream):
 * move → apply the landing cell (Strategy via Registry) → detect winner → rotate turn.
 * Stateless: it only mutates the entities it receives.
 */
@Component
@RequiredArgsConstructor
public class TurnEngine {

    private final CellEffectRegistry cellEffectRegistry;

    /** Moves the player, applies the landing cell and returns the history entry. */
    public Move play(Game game, Player player, int diceValue) {
        int from = player.getPosition();
        int landing = Math.min(from + diceValue, game.getGoalPosition());
        player.setPosition(landing);

        // cells are ordered by position (@OrderBy) and built one per position (BoardFactory)
        CellType landedType = game.getCells().get(landing).getType();
        if (landedType != CellType.GOAL) {
            cellEffectRegistry.get(landedType).apply(game, player);
        }

        Move move = new Move();
        move.setGame(game);
        move.setPlayer(player);
        move.setDiceValue(diceValue);
        move.setFromPosition(from);
        move.setToPosition(player.getPosition());
        move.setCellType(landedType);
        return move;
    }

    /**
     * Victory rules: reaching the goal wins; otherwise the last player alive wins.
     */
    public Optional<Player> findWinner(Game game, Player lastMover) {
        if (lastMover.getPosition() == game.getGoalPosition()) {
            return Optional.of(lastMover);
        }
        List<Player> alive = game.getPlayers().stream()
                .filter(Player::isAlive)
                .toList();
        if (alive.size() == 1) {
            return Optional.of(alive.get(0));
        }
        return Optional.empty();
    }

    /**
     * Passes the turn to the next alive player. A player flagged with skipNextTurn
     * loses this rotation (the flag is consumed). Two laps are enough: the first
     * lap consumes every flag, the second one always finds an alive player.
     */
    public void advanceTurn(Game game) {
        List<Player> players = game.getPlayers();
        int size = players.size();
        int index = game.getCurrentTurn();
        for (int step = 0; step < size * 2; step++) {
            index = (index + 1) % size;
            Player candidate = players.get(index);
            if (!candidate.isAlive()) {
                continue;
            }
            if (candidate.isSkipNextTurn()) {
                candidate.setSkipNextTurn(false);
                continue;
            }
            game.setCurrentTurn(index);
            return;
        }
    }
}
