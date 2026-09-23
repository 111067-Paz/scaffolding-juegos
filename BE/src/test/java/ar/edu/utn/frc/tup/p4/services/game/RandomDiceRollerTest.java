package ar.edu.utn.frc.tup.p4.services.game;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("unit")
@DisplayName("RandomDiceRoller")
class RandomDiceRollerTest {

    private final RandomDiceRoller diceRoller = new RandomDiceRoller();

    @RepeatedTest(50)
    @DisplayName("roll always returns a value between 1 and 6 (limits)")
    void roll_always_returnsValueWithinFaces() {
        int value = diceRoller.roll();
        assertTrue(value >= 1 && value <= DiceRoller.FACES, "out of range: " + value);
    }
}
