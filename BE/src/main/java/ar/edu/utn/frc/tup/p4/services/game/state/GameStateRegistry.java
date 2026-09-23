package ar.edu.utn.frc.tup.p4.services.game.state;

import ar.edu.utn.frc.tup.p4.entities.GameStatus;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Registry: GameStatus → GameState, built once from every GameState bean. */
@Component
public class GameStateRegistry {

    private final Map<GameStatus, GameState> states = new EnumMap<>(GameStatus.class);

    public GameStateRegistry(List<GameState> stateList) {
        for (GameState state : stateList) {
            states.put(state.supports(), state);
        }
    }

    public GameState get(GameStatus status) {
        GameState state = states.get(status);
        if (state == null) {
            throw new IllegalStateException("No state registered for status " + status);
        }
        return state;
    }
}
