package ar.edu.utn.frc.tup.p4.mappers;

import ar.edu.utn.frc.tup.p4.dtos.CellDTO;
import ar.edu.utn.frc.tup.p4.dtos.GameDTO;
import ar.edu.utn.frc.tup.p4.dtos.GameSummaryDTO;
import ar.edu.utn.frc.tup.p4.dtos.MoveDTO;
import ar.edu.utn.frc.tup.p4.dtos.PlayerDTO;
import ar.edu.utn.frc.tup.p4.entities.Cell;
import ar.edu.utn.frc.tup.p4.entities.Game;
import ar.edu.utn.frc.tup.p4.entities.GameStatus;
import ar.edu.utn.frc.tup.p4.entities.Move;
import ar.edu.utn.frc.tup.p4.entities.Player;
import org.springframework.stereotype.Component;

/**
 * Manual mapper (explicit, testable, no reflection). Injected into the service,
 * never instantiated there. Streams here: pure entity → DTO transformations.
 * Precondition: the service already loaded players/cells (JOIN FETCH) — the
 * mapper never triggers lazy loading on its own.
 */
@Component
public class GameMapper {

    public GameDTO toDTO(Game game) {
        return GameDTO.builder()
                .id(game.getId())
                .status(game.getStatus())
                .boardSize(game.getBoardSize())
                .currentPlayerId(game.getStatus() == GameStatus.IN_PROGRESS
                        ? game.getCurrentPlayer().getId() : null)
                .winnerId(game.getWinner() != null ? game.getWinner().getId() : null)
                .players(game.getPlayers().stream().map(this::toPlayerDTO).toList())
                .cells(game.getCells().stream().map(this::toCellDTO).toList())
                .createdAt(game.getCreatedAt())
                .build();
    }

    public GameSummaryDTO toSummaryDTO(Game game) {
        return GameSummaryDTO.builder()
                .id(game.getId())
                .status(game.getStatus())
                .boardSize(game.getBoardSize())
                .winnerName(game.getWinner() != null ? game.getWinner().getName() : null)
                .createdAt(game.getCreatedAt())
                .build();
    }

    public PlayerDTO toPlayerDTO(Player player) {
        return PlayerDTO.builder()
                .id(player.getId())
                .name(player.getName())
                .turnOrder(player.getTurnOrder())
                .position(player.getPosition())
                .lives(player.getLives())
                .skipNextTurn(player.isSkipNextTurn())
                .alive(player.isAlive())
                .build();
    }

    public CellDTO toCellDTO(Cell cell) {
        return CellDTO.builder()
                .position(cell.getPosition())
                .type(cell.getType())
                .build();
    }

    public MoveDTO toMoveDTO(Move move) {
        return MoveDTO.builder()
                .id(move.getId())
                .playerId(move.getPlayer().getId())
                .playerName(move.getPlayer().getName())
                .diceValue(move.getDiceValue())
                .fromPosition(move.getFromPosition())
                .toPosition(move.getToPosition())
                .cellType(move.getCellType())
                .createdAt(move.getCreatedAt())
                .build();
    }
}
