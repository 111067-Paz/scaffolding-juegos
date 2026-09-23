package ar.edu.utn.frc.tup.p4.services.game.effects;

import ar.edu.utn.frc.tup.p4.entities.CellType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Registry: CellType → CellEffect, built ONCE from every CellEffect bean.
 * A new cell type = a new @Component; this class and the engine do not change (Open/Closed).
 */
@Component
public class CellEffectRegistry {

    private final Map<CellType, CellEffect> effects = new EnumMap<>(CellType.class);

    public CellEffectRegistry(List<CellEffect> effectList) {
        for (CellEffect effect : effectList) {
            effects.put(effect.supports(), effect);
        }
    }

    public CellEffect get(CellType type) {
        CellEffect effect = effects.get(type);
        if (effect == null) {
            throw new IllegalStateException("No effect registered for cell type " + type);
        }
        return effect;
    }
}
