package ar.edu.utn.frc.tup.p4.services.game.effects;

import ar.edu.utn.frc.tup.p4.entities.CellType;
import ar.edu.utn.frc.tup.p4.entities.Game;
import ar.edu.utn.frc.tup.p4.entities.Player;

/**
 * Strategy: what happens when a player lands on a cell.
 * ONE operation, one implementation per CellType. Implementations are stateless
 * beans: they mutate the entities they receive; the service persists them.
 */
public interface CellEffect {

    CellType supports();

    void apply(Game game, Player player);
}
