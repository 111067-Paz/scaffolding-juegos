package ar.edu.utn.frc.tup.p4.dtos;

import ar.edu.utn.frc.tup.p4.entities.CellType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoveDTO {

    private Long id;

    @JsonProperty("player_id")
    private Long playerId;

    @JsonProperty("player_name")
    private String playerName;

    @JsonProperty("dice_value")
    private Integer diceValue;

    @JsonProperty("from_position")
    private Integer fromPosition;

    @JsonProperty("to_position")
    private Integer toPosition;

    @JsonProperty("cell_type")
    private CellType cellType;

    @JsonProperty("created_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime createdAt;
}
