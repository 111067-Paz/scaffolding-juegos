package ar.edu.utn.frc.tup.p4.services.game.effects;

import ar.edu.utn.frc.tup.p4.entities.CellType;
import ar.edu.utn.frc.tup.p4.entities.Game;
import ar.edu.utn.frc.tup.p4.entities.Player;
import org.springframework.stereotype.Component;

/** Costs one life. A player with 0 lives is eliminated (skipped by the turn rotation). */
@Component
public class TrapCellEffect implements CellEffect {

    @Override
    public CellType supports() {
        return CellType.TRAP;
    }

    @Override
    public void apply(Game game, Player player) {
        player.setLives(Math.max(player.getLives() - 1, 0));
    }
}
