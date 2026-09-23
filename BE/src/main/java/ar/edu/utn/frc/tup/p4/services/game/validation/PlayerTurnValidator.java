package ar.edu.utn.frc.tup.p4.services.game.validation;

import ar.edu.utn.frc.tup.p4.entities.Game;
import ar.edu.utn.frc.tup.p4.entities.Player;
import ar.edu.utn.frc.tup.p4.exceptions.InvalidMoveException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** Link 2: only the player whose turn it is may roll. */
@Component
@Order(2)
public class PlayerTurnValidator implements MoveValidator {

    @Override
    public void validate(Game game, Player player) {
        Player current = game.getCurrentPlayer();
        if (!current.getId().equals(player.getId())) {
            throw new InvalidMoveException("It is not " + player.getName() + "'s turn, it is "
                    + current.getName() + "'s turn");
        }
    }
}
