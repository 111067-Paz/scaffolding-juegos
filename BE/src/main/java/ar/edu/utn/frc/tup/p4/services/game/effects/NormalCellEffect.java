package ar.edu.utn.frc.tup.p4.services.game.effects;

import ar.edu.utn.frc.tup.p4.entities.CellType;
import ar.edu.utn.frc.tup.p4.entities.Game;
import ar.edu.utn.frc.tup.p4.entities.Player;
import org.springframework.stereotype.Component;

/** Plain cell: nothing happens (Null Object — avoids a null check in the engine). */
@Component
public class NormalCellEffect implements CellEffect {

    @Override
    public CellType supports() {
        return CellType.NORMAL;
    }

    @Override
    public void apply(Game game, Player player) {
        // Intentionally empty
    }
}
