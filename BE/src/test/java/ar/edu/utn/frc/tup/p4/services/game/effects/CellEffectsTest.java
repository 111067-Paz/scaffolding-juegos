package ar.edu.utn.frc.tup.p4.services.game.effects;

import ar.edu.utn.frc.tup.p4.entities.CellType;
import ar.edu.utn.frc.tup.p4.entities.Game;
import ar.edu.utn.frc.tup.p4.entities.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static ar.edu.utn.frc.tup.p4.support.GameFixtures.gameInProgress;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** One test per strategy + its limits (stateless beans → plain instances, no mocks). */
@Tag("unit")
@DisplayName("CellEffect strategies")
class CellEffectsTest {

    private Game game;
    private Player player;

    @BeforeEach
    void setUp() {
        game = gameInProgress(20, "Ana", "Beto");
        player = game.getPlayers().get(0);
        player.setPosition(10);
    }

    @Test
    @DisplayName("NORMAL changes nothing")
    void normal_apply_changesNothing() {
        NormalCellEffect effect = new NormalCellEffect();
        effect.apply(game, player);
        assertEquals(CellType.NORMAL, effect.supports());
        assertEquals(10, player.getPosition());
        assertEquals(3, player.getLives());
    }

    @Test
    @DisplayName("ADVANCE moves forward and never passes the goal (limit)")
    void advance_apply_movesForwardClampedToGoal() {
        AdvanceCellEffect effect = new AdvanceCellEffect();
        effect.apply(game, player);
        assertEquals(CellType.ADVANCE, effect.supports());
        assertEquals(10 + AdvanceCellEffect.STEPS, player.getPosition());

        player.setPosition(18);
        effect.apply(game, player);
        assertEquals(19, player.getPosition());
    }

    @Test
    @DisplayName("BACK moves backwards and never below 0 (limit)")
    void back_apply_movesBackClampedToStart() {
        BackCellEffect effect = new BackCellEffect();
        effect.apply(game, player);
        assertEquals(CellType.BACK, effect.supports());
        assertEquals(10 - BackCellEffect.STEPS, player.getPosition());

        player.setPosition(1);
        effect.apply(game, player);
        assertEquals(0, player.getPosition());
    }

    @Test
    @DisplayName("LOSE_TURN flags the player to skip the next turn")
    void loseTurn_apply_setsSkipFlag() {
        LoseTurnCellEffect effect = new LoseTurnCellEffect();
        assertFalse(player.isSkipNextTurn());
        effect.apply(game, player);
        assertEquals(CellType.LOSE_TURN, effect.supports());
        assertTrue(player.isSkipNextTurn());
    }

    @Test
    @DisplayName("TRAP takes one life and never goes below 0 (limit)")
    void trap_apply_takesOneLifeClampedToZero() {
        TrapCellEffect effect = new TrapCellEffect();
        effect.apply(game, player);
        assertEquals(CellType.TRAP, effect.supports());
        assertEquals(2, player.getLives());

        player.setLives(0);
        effect.apply(game, player);
        assertEquals(0, player.getLives());
        assertFalse(player.isAlive());
    }
}
