package ar.edu.utn.frc.tup.p4.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerDTO {

    private Long id;

    private String name;

    @JsonProperty("turn_order")
    private Integer turnOrder;

    private Integer position;

    private Integer lives;

    @JsonProperty("skip_next_turn")
    private boolean skipNextTurn;

    private boolean alive;
}
