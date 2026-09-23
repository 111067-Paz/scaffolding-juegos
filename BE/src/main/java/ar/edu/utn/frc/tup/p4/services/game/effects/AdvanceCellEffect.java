package ar.edu.utn.frc.tup.p4.services.game.effects;

import ar.edu.utn.frc.tup.p4.entities.CellType;
import ar.edu.utn.frc.tup.p4.entities.Game;
import ar.edu.utn.frc.tup.p4.entities.Player;
import org.springframework.stereotype.Component;

/** Moves the player forward. Effects do not chain (landing cell effect is not re-applied). */
@Component
public class AdvanceCellEffect implements CellEffect {

    static final int STEPS = 2;

    @Override
    public CellType supports() {
        return CellType.ADVANCE;
    }

    @Override
    public void apply(Game game, Player player) {
        player.setPosition(Math.min(player.getPosition() + STEPS, game.getGoalPosition()));
    }
}
