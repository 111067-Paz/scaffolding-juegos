package ar.edu.utn.frc.tup.p4.services.game;

import ar.edu.utn.frc.tup.p4.entities.CellType;
import ar.edu.utn.frc.tup.p4.entities.Game;
import ar.edu.utn.frc.tup.p4.entities.Move;
import ar.edu.utn.frc.tup.p4.entities.Player;
import ar.edu.utn.frc.tup.p4.services.game.effects.CellEffect;
import ar.edu.utn.frc.tup.p4.services.game.effects.CellEffectRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static ar.edu.utn.frc.tup.p4.support.GameFixtures.gameInProgress;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("TurnEngine")
class TurnEngineTest {

    @Mock
    private CellEffectRegistry cellEffectRegistry;

    @Mock
    private CellEffect cellEffect;

    @InjectMocks
    private TurnEngine turnEngine;

    @Test
    @DisplayName("play moves the player, applies the landing cell effect and records the move")
    void play_onNormalCell_movesAndAppliesEffect() {
        // GIVEN
        Game game = gameInProgress(10, "Ana", "Beto");
        Player ana = game.getPlayers().get(0);
        when(cellEffectRegistry.get(CellType.NORMAL)).thenReturn(cellEffect);

        // WHEN
        Move move = turnEngine.play(game, ana, 4);

        // THEN
        assertEquals(4, ana.getPosition());
        verify(cellEffect).apply(game, ana);
        assertEquals(0, move.getFromPosition());
        assertEquals(4, move.getToPosition());
        assertEquals(4, move.getDiceValue());
        assertEquals(CellType.NORMAL, move.getCellType());
        assertSame(ana, move.getPlayer());
    }

    @Test
    @DisplayName("play clamps the move to the goal and never applies an effect on GOAL (limit)")
    void play_beyondGoal_clampsToGoalWithoutEffect() {
        // GIVEN
        Game game = gameInProgress(10, "Ana", "Beto");
        Player ana = game.getPlayers().get(0);
        ana.setPosition(7);

        // WHEN
        Move move = turnEngine.play(game, ana, 6);

        // THEN
        assertEquals(9, ana.getPosition());
        assertEquals(CellType.GOAL, move.getCellType());
        verify(cellEffectRegistry, never()).get(any());
    }

    @Test
    @DisplayName("findWinner returns the mover when they reach the goal")
    void findWinner_whenOnGoal_returnsMover() {
        Game game = gameInProgress(10, "Ana", "Beto");
        Player ana = game.getPlayers().get(0);
        ana.setPosition(9);

        assertEquals(Optional.of(ana), turnEngine.findWinner(game, ana));
    }

    @Test
    @DisplayName("findWinner returns the last player alive")
    void findWinner_whenOnlyOneAlive_returnsSurvivor() {
        Game game = gameInProgress(10, "Ana", "Beto");
        Player ana = game.getPlayers().get(0);
        Player beto = game.getPlayers().get(1);
        ana.setLives(0);

        assertEquals(Optional.of(beto), turnEngine.findWinner(game, ana));
    }

    @Test
    @DisplayName("findWinner returns empty while the race goes on")
    void findWinner_whenNobodyWon_returnsEmpty() {
        Game game = gameInProgress(10, "Ana", "Beto");

        assertFalse(turnEngine.findWinner(game, game.getPlayers().get(0)).isPresent());
    }

    @Test
    @DisplayName("advanceTurn passes the turn to the next player (wraps around)")
    void advanceTurn_fromLastPlayer_wrapsToFirst() {
        Game game = gameInProgress(10, "Ana", "Beto", "Caro");
        game.setCurrentTurn(2);

        turnEngine.advanceTurn(game);

        assertEquals(0, game.getCurrentTurn());
    }

    @Test
    @DisplayName("advanceTurn skips eliminated players and consumes the skip flag")
    void advanceTurn_withEliminatedAndSkippingPlayers_picksNextEligible() {
        // GIVEN — Beto eliminated, Caro loses this turn
        Game game = gameInProgress(10, "Ana", "Beto", "Caro");
        game.getPlayers().get(1).setLives(0);
        Player caro = game.getPlayers().get(2);
        caro.setSkipNextTurn(true);

        // WHEN
        turnEngine.advanceTurn(game);

        // THEN
        assertEquals(0, game.getCurrentTurn());
        assertFalse(caro.isSkipNextTurn());
    }

    @Test
    @DisplayName("advanceTurn when every other player is skipping comes back to the current player")
    void advanceTurn_whenOthersSkip_returnsToCurrent() {
        Game game = gameInProgress(10, "Ana", "Beto");
        game.getPlayers().get(1).setSkipNextTurn(true);

        turnEngine.advanceTurn(game);

        assertEquals(0, game.getCurrentTurn());
        assertTrue(game.getPlayers().get(0).isAlive());
    }

    @Test
    @DisplayName("advanceTurn with no eligible player leaves the turn unchanged (defensive limit)")
    void advanceTurn_whenNobodyAlive_keepsTurn() {
        Game game = gameInProgress(10, "Ana", "Beto");
        game.getPlayers().forEach(player -> player.setLives(0));
        game.setCurrentTurn(1);

        turnEngine.advanceTurn(game);

        assertEquals(1, game.getCurrentTurn());
    }
}
