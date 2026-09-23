package ar.edu.utn.frc.tup.p4.services.game.state;

import ar.edu.utn.frc.tup.p4.entities.Game;
import ar.edu.utn.frc.tup.p4.entities.GameStatus;
import ar.edu.utn.frc.tup.p4.entities.Move;
import ar.edu.utn.frc.tup.p4.entities.Player;

/**
 * State: each game phase decides which actions are valid and how they behave.
 * The phase itself is PERSISTED (Game.status); these beans are stateless.
 * The service asks the registry for the state of the current status and delegates:
 * no switch(status) anywhere.
 */
public interface GameState {

    GameStatus supports();

    void start(Game game);

    Move roll(Game game, Player player);
}
