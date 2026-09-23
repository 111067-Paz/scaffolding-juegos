package ar.edu.utn.frc.tup.p4.dtos;

import ar.edu.utn.frc.tup.p4.entities.GameStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Lightweight row for "my games" (no players/cells → a single query). */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameSummaryDTO {

    private Long id;

    private GameStatus status;

    @JsonProperty("board_size")
    private Integer boardSize;

    @JsonProperty("winner_name")
    private String winnerName;

    @JsonProperty("created_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime createdAt;
}
