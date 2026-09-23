package ar.edu.utn.frc.tup.p4.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Create a hot-seat game. The owner is NOT in the body: it comes from the token.
 * The player-name regex is IDENTICAL to the one in the Angular form (game-new-page.ts).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameCreateRequest {

    @NotNull(message = "Player names are required")
    @Size(min = 2, max = 4, message = "A game needs between 2 and 4 players")
    @JsonProperty("player_names")
    private List<@NotBlank(message = "Player name is required")
            @Pattern(regexp = "^[A-Za-zÁÉÍÓÚáéíóúÑñ0-9 ]{2,30}$",
                    message = "Player name must have 2-30 letters, numbers or spaces") String> playerNames;

    @NotNull(message = "Board size is required")
    @Min(value = 10, message = "Board size must be at least 10")
    @Max(value = 60, message = "Board size must be at most 60")
    @JsonProperty("board_size")
    private Integer boardSize;
}
