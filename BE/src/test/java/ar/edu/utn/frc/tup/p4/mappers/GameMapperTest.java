package ar.edu.utn.frc.tup.p4.mappers;

import ar.edu.utn.frc.tup.p4.dtos.GameDTO;
import ar.edu.utn.frc.tup.p4.dtos.GameSummaryDTO;
import ar.edu.utn.frc.tup.p4.dtos.MoveDTO;
import ar.edu.utn.frc.tup.p4.entities.CellType;
import ar.edu.utn.frc.tup.p4.entities.Game;
import ar.edu.utn.frc.tup.p4.entities.GameStatus;
import ar.edu.utn.frc.tup.p4.entities.Move;
import ar.edu.utn.frc.tup.p4.entities.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static ar.edu.utn.frc.tup.p4.support.GameFixtures.gameInProgress;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

@Tag("unit")
@DisplayName("GameMapper")
class GameMapperTest {

    private final GameMapper mapper = new GameMapper();

    @Test
    @DisplayName("toDTO maps players, cells and the current player while IN_PROGRESS")
    void toDTO_inProgress_mapsEverything() {
        Game game = gameInProgress(10, "Ana", "Beto");
        game.setCurrentTurn(1);
        game.getPlayers().get(0).setLives(0);

        GameDTO dto = mapper.toDTO(game);

        assertEquals(game.getId(), dto.getId());
        assertEquals(GameStatus.IN_PROGRESS, dto.getStatus());
        assertEquals(101L, dto.getCurrentPlayerId());
        assertNull(dto.getWinnerId());
        assertEquals(2, dto.getPlayers().size());
        assertFalse(dto.getPlayers().get(0).isAlive());
        assertEquals(10, dto.getCells().size());
        assertEquals(CellType.GOAL, dto.getCells().get(9).getType());
    }

    @Test
    @DisplayName("toDTO of a FINISHED game has no current player but has a winner")
    void toDTO_finished_mapsWinnerWithoutCurrentPlayer() {
        Game game = gameInProgress(10, "Ana", "Beto");
        game.setStatus(GameStatus.FINISHED);
        game.setWinner(game.getPlayers().get(1));

        GameDTO dto = mapper.toDTO(game);

        assertNull(dto.getCurrentPlayerId());
        assertEquals(101L, dto.getWinnerId());
    }

    @Test
    @DisplayName("toSummaryDTO maps the winner name only when there is a winner")
    void toSummaryDTO_withAndWithoutWinner() {
        Game game = gameInProgress(10, "Ana", "Beto");
        assertNull(mapper.toSummaryDTO(game).getWinnerName());

        game.setWinner(game.getPlayers().get(0));
        GameSummaryDTO dto = mapper.toSummaryDTO(game);

        assertEquals("Ana", dto.getWinnerName());
        assertEquals(10, dto.getBoardSize());
    }

    @Test
    @DisplayName("toMoveDTO flattens the player into id + name")
    void toMoveDTO_mapsPlayerAndPositions() {
        Game game = gameInProgress(10, "Ana", "Beto");
        Player ana = game.getPlayers().get(0);
        Move move = new Move(5L, game, ana, 3, 0, 3, CellType.NORMAL, LocalDateTime.now());

        MoveDTO dto = mapper.toMoveDTO(move);

        assertEquals(5L, dto.getId());
        assertEquals(100L, dto.getPlayerId());
        assertEquals("Ana", dto.getPlayerName());
        assertEquals(3, dto.getToPosition());
        assertEquals(CellType.NORMAL, dto.getCellType());
    }
}
