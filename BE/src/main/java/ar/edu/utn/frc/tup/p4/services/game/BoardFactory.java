package ar.edu.utn.frc.tup.p4.services.game;

import ar.edu.utn.frc.tup.p4.entities.Cell;
import ar.edu.utn.frc.tup.p4.entities.CellType;
import ar.edu.utn.frc.tup.p4.entities.Game;
import org.springframework.stereotype.Component;

/**
 * Factory: builds the board in ONE testable place.
 * Deterministic layout (same size → same board) so games are reproducible.
 * Adapting to the exam: replace resolveType() with the rules of the statement.
 */
@Component
public class BoardFactory {

    public void createBoard(Game game) {
        int size = game.getBoardSize();
        // Traditional loop: builds cells in order, one per position.
        for (int position = 0; position < size; position++) {
            Cell cell = new Cell();
            cell.setPosition(position);
            cell.setType(resolveType(position, size));
            game.addCell(cell);
        }
    }

    CellType resolveType(int position, int size) {
        if (position == 0) {
            return CellType.NORMAL;
        }
        if (position == size - 1) {
            return CellType.GOAL;
        }
        if (position % 7 == 0) {
            return CellType.TRAP;
        }
        if (position % 5 == 0) {
            return CellType.BACK;
        }
        if (position % 6 == 0) {
            return CellType.LOSE_TURN;
        }
        if (position % 4 == 0) {
            return CellType.ADVANCE;
        }
        return CellType.NORMAL;
    }
}
