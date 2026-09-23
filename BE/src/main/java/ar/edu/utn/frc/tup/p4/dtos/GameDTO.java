package ar.edu.utn.frc.tup.p4.dtos;

import ar.edu.utn.frc.tup.p4.entities.GameStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Full game view: players + board. Nested DTOs → no JPA recursion in JSON.
 * JSON keys are forced with @JsonProperty (the contract is snake_case) and each
 * date declares its own format (no global Jackson date config).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameDTO {

    private Long id;

    private GameStatus status;

    @JsonProperty("board_size")
    private Integer boardSize;

    @JsonProperty("current_player_id")
    private Long currentPlayerId;

    @JsonProperty("winner_id")
    private Long winnerId;

    private List<PlayerDTO> players;

    private List<CellDTO> cells;

    @JsonProperty("created_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime createdAt;
}
