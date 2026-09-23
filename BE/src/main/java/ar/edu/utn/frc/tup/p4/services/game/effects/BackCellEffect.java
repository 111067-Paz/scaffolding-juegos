package ar.edu.utn.frc.tup.p4.services.game.effects;

import ar.edu.utn.frc.tup.p4.entities.CellType;
import ar.edu.utn.frc.tup.p4.entities.Game;
import ar.edu.utn.frc.tup.p4.entities.Player;
import org.springframework.stereotype.Component;

/** Moves the player back, never below the start cell. */
@Component
public class BackCellEffect implements CellEffect {

    static final int STEPS = 3;

    @Override
    public CellType supports() {
        return CellType.BACK;
    }

    @Override
    public void apply(Game game, Player player) {
        player.setPosition(Math.max(player.getPosition() - STEPS, 0));
    }
}
