package ar.edu.utn.frc.tup.p4.services.game.validation;

import ar.edu.utn.frc.tup.p4.entities.Game;
import ar.edu.utn.frc.tup.p4.entities.Player;
import ar.edu.utn.frc.tup.p4.exceptions.InvalidMoveException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** Link 1: an eliminated player (0 lives) can not move. */
@Component
@Order(1)
public class PlayerAliveValidator implements MoveValidator {

    @Override
    public void validate(Game game, Player player) {
        if (!player.isAlive()) {
            throw new InvalidMoveException("Player " + player.getName() + " is eliminated");
        }
    }
}
