package ar.edu.utn.frc.tup.p4.services.game.effects;

import ar.edu.utn.frc.tup.p4.entities.CellType;
import ar.edu.utn.frc.tup.p4.entities.Game;
import ar.edu.utn.frc.tup.p4.entities.Player;
import org.springframework.stereotype.Component;

/** The player skips their next turn (consumed by the TurnEngine when rotating turns). */
@Component
public class LoseTurnCellEffect implements CellEffect {

    @Override
    public CellType supports() {
        return CellType.LOSE_TURN;
    }

    @Override
    public void apply(Game game, Player player) {
        player.setSkipNextTurn(true);
    }
}
