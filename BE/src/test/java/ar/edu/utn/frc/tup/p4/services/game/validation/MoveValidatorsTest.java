package ar.edu.utn.frc.tup.p4.services.game.validation;

import ar.edu.utn.frc.tup.p4.entities.Game;
import ar.edu.utn.frc.tup.p4.entities.Player;
import ar.edu.utn.frc.tup.p4.exceptions.InvalidMoveException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static ar.edu.utn.frc.tup.p4.support.GameFixtures.gameInProgress;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("unit")
@DisplayName("Move validators (chain links)")
class MoveValidatorsTest {

    private final PlayerAliveValidator aliveValidator = new PlayerAliveValidator();
    private final PlayerTurnValidator turnValidator = new PlayerTurnValidator();

    @Test
    @DisplayName("PlayerAliveValidator lets an alive player through")
    void alive_whenPlayerHasLives_passes() {
        Game game = gameInProgress(10, "Ana", "Beto");
        assertDoesNotThrow(() -> aliveValidator.validate(game, game.getPlayers().get(0)));
    }

    @Test
    @DisplayName("PlayerAliveValidator rejects a player with 0 lives (limit)")
    void alive_whenPlayerHasZeroLives_throws() {
        Game game = gameInProgress(10, "Ana", "Beto");
        Player ana = game.getPlayers().get(0);
        ana.setLives(0);

        InvalidMoveException exception = assertThrows(InvalidMoveException.class,
                () -> aliveValidator.validate(game, ana));
        assertEquals("Player Ana is eliminated", exception.getMessage());
    }

    @Test
    @DisplayName("PlayerTurnValidator lets the current player through")
    void turn_whenCurrentPlayer_passes() {
        Game game = gameInProgress(10, "Ana", "Beto");
        assertDoesNotThrow(() -> turnValidator.validate(game, game.getPlayers().get(0)));
    }

    @Test
    @DisplayName("PlayerTurnValidator rejects a player out of turn")
    void turn_whenNotCurrentPlayer_throws() {
        Game game = gameInProgress(10, "Ana", "Beto");

        InvalidMoveException exception = assertThrows(InvalidMoveException.class,
                () -> turnValidator.validate(game, game.getPlayers().get(1)));
        assertEquals("It is not Beto's turn, it is Ana's turn", exception.getMessage());
    }
}
