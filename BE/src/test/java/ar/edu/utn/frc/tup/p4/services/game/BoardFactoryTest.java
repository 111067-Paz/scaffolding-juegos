package ar.edu.utn.frc.tup.p4.services.game;

import ar.edu.utn.frc.tup.p4.entities.CellType;
import ar.edu.utn.frc.tup.p4.entities.Game;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

@Tag("unit")
@DisplayName("BoardFactory")
class BoardFactoryTest {

    private final BoardFactory boardFactory = new BoardFactory();

    @Test
    @DisplayName("createBoard creates one cell per position, linked to the game, ending in GOAL")
    void createBoard_withSize10_createsTenOrderedCellsEndingInGoal() {
        // GIVEN
        Game game = new Game();
        game.setBoardSize(10);

        // WHEN
        boardFactory.createBoard(game);

        // THEN
        assertEquals(10, game.getCells().size());
        assertEquals(CellType.NORMAL, game.getCells().get(0).getType());
        assertEquals(CellType.GOAL, game.getCells().get(9).getType());
        assertEquals(5, game.getCells().get(5).getPosition());
        assertSame(game, game.getCells().get(5).getGame());
    }

    @ParameterizedTest(name = "position {0} of 30 → {1}")
    @CsvSource({
            "0, NORMAL",     // start is always safe
            "29, GOAL",      // last cell
            "14, TRAP",      // multiple of 7 wins over the rest
            "35, TRAP",      // (limit: checked before %5)
            "10, BACK",
            "12, LOSE_TURN",
            "8, ADVANCE",
            "1, NORMAL"
    })
    @DisplayName("resolveType applies the layout rules in priority order")
    void resolveType_eachRule_returnsExpectedType(int position, CellType expected) {
        assertEquals(expected, boardFactory.resolveType(position, position == 35 ? 40 : 30));
    }
}
