package ar.edu.utn.frc.tup.p4.services.game;

import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

/** Stateless six-sided die (thread-safe: ThreadLocalRandom, no shared state). */
@Component
public class RandomDiceRoller implements DiceRoller {

    @Override
    public int roll() {
        return ThreadLocalRandom.current().nextInt(1, FACES + 1);
    }
}
