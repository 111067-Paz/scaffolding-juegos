package ar.edu.utn.frc.tup.p4.services.game.effects;

import ar.edu.utn.frc.tup.p4.entities.CellType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("unit")
@DisplayName("CellEffectRegistry")
class CellEffectRegistryTest {

    private final CellEffectRegistry registry = new CellEffectRegistry(
            List.of(new NormalCellEffect(), new TrapCellEffect()));

    @Test
    @DisplayName("get returns the strategy registered for the type")
    void get_registeredType_returnsStrategy() {
        assertInstanceOf(TrapCellEffect.class, registry.get(CellType.TRAP));
    }

    @Test
    @DisplayName("get throws IllegalStateException for a type without strategy")
    void get_unregisteredType_throws() {
        assertThrows(IllegalStateException.class, () -> registry.get(CellType.BACK));
    }
}
