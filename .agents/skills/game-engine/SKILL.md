---
name: game-engine
description: >
  Núcleo del scaffold de juegos: cómo está armado el juego base (carrera de dados) y cómo
  adaptarlo a cualquier enunciado por turnos. State (fases de la partida), Strategy + Registry
  (tipos de casilla/carta/pregunta), Factory (tablero/mazo), Chain of Responsibility
  (validar jugadas), azar inyectable (DiceRoller), TurnEngine, victoria, vistas por jugador,
  concurrencia con @Version, y el criterio anti-sobreingeniería. El estado del juego SIEMPRE
  persiste en JPA; los patrones son beans sin estado.
  Trigger: SIEMPRE que se diseñe o implemente lógica de juego — partidas, turnos, tablero,
  casillas, cartas, dados, vidas, puntajes, victoria, desempates — o al ver un switch/if
  sobre tipos que puede crecer, o al planificar cualquier juego (batalla naval, UNO, truco,
  ta-te-ti, TCG, trivia), aunque el usuario no diga "patrón".
license: Apache-2.0
metadata:
  author: PazLuciano
  version: "2.0"
---

# game-engine

## 0. Dos reglas antes que cualquier patrón

1. **El estado vive en entidades JPA.** Strategies, states y validators son `@Component`
   **sin estado**: reciben la entidad, la mutan, el service persiste. Un `Map` estático o un
   singleton con partidas es falta grave (reiniciar no puede perder partidas).
2. **El patrón se justifica por el enunciado.** 3+ variantes hoy, o crecimiento anunciado
   ("en la segunda parte se agregan casillas") → patrón. 2 casos estables → `if`.

## 1. Anatomía del juego base (copiar al lado, no reinventar)

```
services/game/
├── state/        GameState (interfaz) · WaitingState · InProgressState · FinishedState · GameStateRegistry
├── effects/      CellEffect (Strategy) · Normal/Advance/Back/LoseTurn/TrapCellEffect · CellEffectRegistry
├── validation/   MoveValidator (Chain) · PlayerAliveValidator @Order(1) · PlayerTurnValidator @Order(2)
├── BoardFactory  (Factory: arma las casillas según reglas)
├── DiceRoller    (interfaz) · RandomDiceRoller (bean) → en tests se mockea
└── TurnEngine    (reglas de un turno: mover → efecto → ganador → rotar turno)
```

Flujo de `POST /api/games/{id}/roll`:

```
GameServiceImpl.roll
  └─ loadOwnedGame (JOIN FETCH + ownership)
  └─ gameStateRegistry.get(game.getStatus()).roll(game, player)     ← State
        InProgressState: validators.forEach(validate)                ← Chain
                         turnEngine.play(game, player, dice.roll())  ← Strategy vía Registry
                         findWinner → FINISHED | advanceTurn
  └─ moveRepository.save(move) → RollResultDTO(move, game)
```

## 2. Problema del enunciado → patrón

| Señal en el enunciado | Patrón | En el scaffold |
|---|---|---|
| "La partida puede estar esperando / en curso / terminada" y las acciones valen según la fase | **State** | `GameState` + registry |
| "Hay casillas / cartas / preguntas de distintos tipos" con efecto distinto | **Strategy + Registry** | `CellEffect` + `CellEffectRegistry` |
| "El tablero se arma así: cada N casillas…" / "el mazo tiene…" | **Factory** | `BoardFactory` |
| "Solo puede jugar si es su turno, si está vivo, si la jugada es legal…" | **Chain of Responsibility** | `MoveValidator` + `@Order` |
| Azar (dados, mezclar mazo, sortear turno) | **Interfaz inyectable** | `DiceRoller` |
| Varias reglas de puntaje intercambiables | Strategy | (agregar `ScoringRule`) |

- **State pragmático** (válido si la fase casi no cambia el comportamiento): enum persistido +
  `if (status != IN_PROGRESS) throw new InvalidGameStateException(...)` en el service. Pasar a
  clases State cuando el `if`/`switch` sobre el status aparece en 3+ métodos.
- **Registry**: se construye desde `List<Strategy>` inyectada → agregar un tipo = una clase
  nueva `@Component`, cero cambios en el registry ni en el engine (Open/Closed). Tipo sin
  strategy → `IllegalStateException` (error de programación → 500).
- **Chain**: el orden es explícito con `@Order`; el primero que falla corta (excepción de
  dominio → 409). Cada link valida UNA regla y tiene su test.
- Código de las variantes y resto de GoF (Template Method, Observer, Adapter…):
  [references/patrones-gof.md](references/patrones-gof.md).

## 3. Reglas del turno — código tradicional

El turno es un algoritmo con **orden obligatorio y efectos** → `for`/`if` en `TurnEngine`, no
streams (skill `java21-clean-code`). Las reglas finas (meta exacta o no, efectos que se
encadenan o no, qué pasa al quedarse sin vidas) se documentan en el Javadoc del método y se
fijan con un test por regla.

## 4. Azar y tests deterministas

```java
@MockitoBean
private DiceRoller diceRoller;             // integración: dado fijo
when(diceRoller.roll()).thenReturn(3);     // la partida entera es reproducible
```

Unitarios: el `TurnEngine` recibe el valor del dado como parámetro → se testea sin mocks de azar.

## 5. Concurrencia y seguridad del juego

- `@Version` en `Game`: dos "tirar" simultáneos → el segundo recibe 409.
- Ownership en el service (partida ajena → 404). El jugador que actúa se valida en la chain
  (no confiar en el `player_id` del body sin chequear el turno).
- Información oculta (cartas, barcos): DTO por jugador, nunca el estado completo.

## 6. Adaptar a un enunciado nuevo

Qué se queda, qué se reemplaza según el juego (batalla naval, UNO, ta-te-ti, TCG, trivia,
memotest) y los pasos en orden: [references/adaptar-juegos.md](references/adaptar-juegos.md).
Resumen: renombrar/ajustar entidades → tipos + strategies → factory → TurnEngine → validators
→ tests hermanos → FE (`game.models.ts`, `CELL_META`, `board-grid`).

## 7. Frontend del juego

DOM para juegos por turnos, canvas solo para tiempo real; el servidor decide, el FE dibuja;
smart page + dumb UI; `computed()` para todo lo derivado:
[references/frontend-juegos.md](references/frontend-juegos.md).

## Checklist

- [ ] Cada patrón responde a una frase concreta del enunciado (podés citarla)
- [ ] Beans de patrones sin estado; todo el estado en entidades
- [ ] Sin `switch` sobre tipos que crecen; registry desde `List<...>` inyectada
- [ ] Validators con `@Order` explícito, uno por regla, cada uno con su test
- [ ] Azar detrás de una interfaz; flujo completo testeado con valor fijo
- [ ] `@Version` en la raíz del agregado; partida ajena → 404
- [ ] Reglas de turno/victoria con un test por regla (incluidos los límites)
