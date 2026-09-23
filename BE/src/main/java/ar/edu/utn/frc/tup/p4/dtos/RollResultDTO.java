package ar.edu.utn.frc.tup.p4.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Result of one turn: what happened (move) + the updated game. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RollResultDTO {

    private MoveDTO move;

    private GameDTO game;
}
