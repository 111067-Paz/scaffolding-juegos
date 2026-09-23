package ar.edu.utn.frc.tup.p4.services.game.validation;

import ar.edu.utn.frc.tup.p4.entities.Game;
import ar.edu.utn.frc.tup.p4.entities.Player;

/**
 * Chain of Responsibility: every link validates ONE rule and either throws
 * (cuts the chain) or lets the move through. Spring injects the links as a
 * List already sorted by @Order, so the order is explicit and testable.
 */
public interface MoveValidator {

    void validate(Game game, Player player);
}
