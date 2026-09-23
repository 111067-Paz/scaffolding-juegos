---
name: java21-clean-code
description: >
  Código limpio en Java 21 para el parcial: PROHIBIDO var, no record (clases Lombok), inglés,
  POO estricta, BigDecimal para dinero, elección razonada entre programación tradicional, stream
  o patrón, colecciones por operación dominante, Optional y métodos cortos con guard clauses.
  Trigger: SIEMPRE que se escriba o revise cualquier método Java — lógica de negocio, reglas
  de juego (turnos, dados, puntajes, vidas, victoria), transformaciones de colecciones,
  cálculos o condicionales sobre tipos — por trivial que parezca.
license: Apache-2.0
metadata:
  author: PazLuciano
  version: "2.0"
---

# java21-clean-code

## 1. Reglas de lenguaje (innegociables)

| Regla | Detalle |
|---|---|
| **PROHIBIDO `var`** | Tipo explícito siempre, también en loops y lambdas largas. |
| **NO `record`** | DTOs/modelos como clases Lombok. |
| **PROHIBIDO `@Autowired`** | `private final` + `@RequiredArgsConstructor`. |
| **Dinero = `BigDecimal`** | Nunca `double`/`float`; comparar con `compareTo`, no `equals`. |
| **Switch expressions: sí** | `case A -> ...` para mapear valores **estables**; si los casos crecen → patrón. |
| **Inglés** | Clases, métodos, variables, logs, comentarios, Javadoc. |
| **Logs** | `@Slf4j` + `log.info/warn/error`; nunca `System.out.println` ni `printStackTrace`. |
| **Sin código comentado** | El historial de git guarda lo viejo. |

## 2. ¿Tradicional, stream o patrón? — decidir por la naturaleza del problema

```
¿Transformación pura de una colección (map/filter/reduce), sin efectos ni orden obligatorio?
    → STREAM                      ej. GameMapper.toDTO, GameServiceImpl.findMine
¿Algoritmo con pasos ORDENADOS, acumulador mutable, efectos o cortes (continue/return)?
    → TRADICIONAL (for / if)      ej. TurnEngine.advanceTurn, BoardFactory.createBoard
¿Un if/switch sobre TIPOS que crece con cada caso nuevo (3+ hoy o anunciado)?
    → PATRÓN (Strategy + Registry, State, Chain) — ver skill game-engine
```

- **NUNCA** mutar estado externo dentro de un stream (`forEach` con `counter++` afuera).
- Un `if` de 2 casos estables no necesita patrón.
- Detalle con casos reales: [references/decisiones-y-colecciones.md](references/decisiones-y-colecciones.md).

```java
// ✅ Tradicional: orden y efectos importan (consume el flag, corta al encontrar)
for (int step = 0; step < size * 2; step++) {
    index = (index + 1) % size;
    Player candidate = players.get(index);
    if (!candidate.isAlive()) {
        continue;
    }
    if (candidate.isSkipNextTurn()) {
        candidate.setSkipNextTurn(false);
        continue;
    }
    game.setCurrentTurn(index);
    return;
}

// ✅ Stream: transformación pura
List<Player> alive = game.getPlayers().stream().filter(Player::isAlive).toList();
```

## 3. Colecciones

| Necesito | Estructura |
|---|---|
| Orden + índice (turnos, casillas por posición) | `ArrayList` / `List` |
| ¿Existe? dentro de un loop | `HashSet` (`List.contains` en un loop = O(n²)) |
| Tipo → comportamiento (registry) | `EnumMap` si la clave es enum, si no `HashMap` |
| Cola de turnos / eventos FIFO | `ArrayDeque` |
| Orden natural por clave (ranking) | `TreeMap` o sort en la **query** |

Declarar por interfaz (`List<Player>`, `Map<CellType, CellEffect>`).

## 4. Nulls y Optional

- `Optional` solo como **retorno** (`findById`, `findWinner`); encadenar con
  `orElseThrow(() -> new GameNotFoundException(id))`, `map`, `isPresent`.
- Nunca `Optional` como campo o parámetro; nunca `optional.get()` sin chequear.
- Ternario para un null trivial en mappers (`winner != null ? winner.getId() : null`).

## 5. Métodos limpios

- Un método = una cosa; ~20 líneas como guía; extraer `private` con nombre expresivo.
- Guard clauses en lugar de `if` anidados.
- Constantes `static final` en MAYÚSCULAS, cero números mágicos
  (`INITIAL_LIVES`, `AdvanceCellEffect.STEPS`, `DiceRoller.FACES`).
- Visibilidad mínima: package-private para lo que solo testea el mismo paquete
  (`BoardFactory.resolveType`).

## Checklist

- [ ] Cero `var`, `record`, `@Autowired`, `System.out`
- [ ] Cada método: ¿stream, tradicional o patrón? — elegido por la naturaleza del problema
- [ ] Ningún stream con efectos externos
- [ ] Colecciones declaradas por interfaz y elegidas por operación dominante
- [ ] `Optional` solo en retornos; sin `.get()` ciego
- [ ] Sin números mágicos; métodos cortos con guard clauses; todo en inglés
