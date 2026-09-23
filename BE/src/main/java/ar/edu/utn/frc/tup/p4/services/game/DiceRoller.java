package ar.edu.utn.frc.tup.p4.services.game;

/**
 * Randomness behind an interface: production uses RandomDiceRoller,
 * tests inject a fixed value → deterministic, reproducible game tests.
 */
public interface DiceRoller {

    int FACES = 6;

    /** @return a value between 1 and {@link #FACES}, inclusive. */
    int roll();
}
