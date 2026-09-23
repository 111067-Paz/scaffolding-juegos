package ar.edu.utn.frc.tup.p4.support;

import ar.edu.utn.frc.tup.p4.entities.Cell;
import ar.edu.utn.frc.tup.p4.entities.CellType;
import ar.edu.utn.frc.tup.p4.entities.Game;
import ar.edu.utn.frc.tup.p4.entities.GameStatus;
import ar.edu.utn.frc.tup.p4.entities.Player;
import ar.edu.utn.frc.tup.p4.entities.User;

/**
 * Test data builders shared by the game unit tests (no Spring, no database).
 * Every cell is NORMAL unless a test overrides it with {@link #setCell}.
 */
public final class GameFixtures {

    public static final Long OWNER_ID = 1L;
    public static final Long GAME_ID = 10L;

    private GameFixtures() {
    }

    /** IN_PROGRESS game with the given players (ids 100, 101, ...) on an all-NORMAL board. */
    public static Game gameInProgress(int boardSize, String... playerNames) {
        User owner = new User();
        owner.setId(OWNER_ID);
        owner.setUsername("owner");

        Game game = new Game();
        game.setId(GAME_ID);
        game.setOwner(owner);
        game.setStatus(GameStatus.IN_PROGRESS);
        game.setBoardSize(boardSize);
        game.setCurrentTurn(0);
        for (int order = 0; order < playerNames.length; order++) {
            Player player = new Player();
            player.setId(100L + order);
            player.setName(playerNames[order]);
            player.setTurnOrder(order);
            player.setPosition(0);
            player.setLives(3);
            game.addPlayer(player);
        }
        for (int position = 0; position < boardSize; position++) {
            Cell cell = new Cell();
            cell.setPosition(position);
            cell.setType(position == boardSize - 1 ? CellType.GOAL : CellType.NORMAL);
            game.addCell(cell);
        }
        return game;
    }

    public static void setCell(Game game, int position, CellType type) {
        game.getCells().get(position).setType(type);
    }
}
