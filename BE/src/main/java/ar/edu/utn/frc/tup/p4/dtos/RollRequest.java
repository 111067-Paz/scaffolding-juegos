package ar.edu.utn.frc.tup.p4.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Roll the dice for a player. The move validators (Chain of Responsibility)
 * check that it really is this player's turn — the server never trusts the client.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RollRequest {

    @NotNull(message = "Player id is required")
    @JsonProperty("player_id")
    private Long playerId;
}
