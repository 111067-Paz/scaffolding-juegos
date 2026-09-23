# Patrones GoF para juegos — versiones de código y referencia rápida

> Referencia de la skill game-engine. Las versiones REALES y testeadas están en el scaffold (services/game/**); acá quedan las variantes (State pragmático con enum, Square/Strategy genérico) y la tabla del resto de GoF.

## 1. State — las fases de la partida

**Cuándo**: la partida se comporta distinto según su fase, y hay transiciones con reglas
(`WAITING → IN_PROGRESS → FINISHED`). ES el patrón central de un juego por turnos.

**Versión pragmática del parcial** (enum persistido + validación de transiciones en el service):

```java
// Entity — el estado PERSISTE como enum
@Enumerated(EnumType.STRING)                      // STRING: legible y no se rompe al reordenar
@Column(name = "status", nullable = false)
private GameStatus status;                        // WAITING, IN_PROGRESS, FINISHED

// Service — TODA transición valida el estado actual ANTES de actuar
@Transactional
public GameDTO rollDice(Long gameId, Long userId) {
    Game game = findOwnedGame(gameId, userId);
    if (game.getStatus() != GameStatus.IN_PROGRESS) {
        throw new InvalidGameStateException("Game " + gameId + " is not in progress");
    }
    // ... lógica del turno ...
}
```

**Versión State completo** (clases por estado) — solo si cada fase tiene MUCHO comportamiento
propio y el enunciado lo insinúa:

```java
public interface GameState {
    GameDTO handleRoll(Game game, int diceValue);   // cada estado implementa SU comportamiento
    GameStatus getStatus();
}

@Component
public class InProgressState implements GameState {
    @Override
    public GameDTO handleRoll(Game game, int diceValue) { /* avanza, aplica casilla, chequea victoria */ }
    @Override
    public GameStatus getStatus() { return GameStatus.IN_PROGRESS; }
}

@Component
public class FinishedState implements GameState {
    @Override
    public GameDTO handleRoll(Game game, int diceValue) {
        throw new InvalidGameStateException("Game already finished");   // estado terminal
    }
}
```

**Regla**: el enum persistido con validación en service YA ES manejo de estado correcto y
suficiente para la mayoría de los parciales. Las clases State se agregan cuando el `switch`
sobre el status crece en 3+ métodos del service.

---

## 2. Strategy — variantes de una misma operación

**Cuándo**: N tipos de casilla/zona/dado/regla, cada uno con SU comportamiento, y la
operación es la misma ("aplicar efecto al caer").

```java
// El contrato — UNA operación, N variantes
public interface SquareEffectStrategy {
    SquareType supports();                          // qué tipo maneja esta strategy
    void apply(Game game, Player player);           // muta la ENTIDAD; el service persiste
}

@Component
public class DamageSquareStrategy implements SquareEffectStrategy {
    @Override
    public SquareType supports() { return SquareType.DAMAGE; }
    @Override
    public void apply(Game game, Player player) {
        player.setLives(Math.max(0, player.getLives() - 1));
    }
}

@Component
public class BonusSquareStrategy implements SquareEffectStrategy {
    @Override
    public SquareType supports() { return SquareType.BONUS; }
    @Override
    public void apply(Game game, Player player) {
        player.setPosition(player.getPosition() + 3);
    }
}
```

**Señales de que Strategy aplica**: el enunciado lista tipos ("casilla normal, de daño,
de bonus, de teletransporte") o la segunda parte agrega tipos nuevos. Con Strategy, la
parte 2 = agregar UNA clase, cero cambios en lo que ya anda.

### ❌ MAL — el switch que crece

```java
// Cada tipo nuevo obliga a REABRIR este método (viola Open/Closed):
switch (square.getType()) {
    case DAMAGE -> player.setLives(player.getLives() - 1);
    case BONUS -> player.setPosition(player.getPosition() + 3);
    case TELEPORT -> { /* ... */ }
    // parte 2: +3 casos más acá adentro...
}
```

---

## 3. Registry — despachar sin switch

**Cuándo**: ya tenés Strategies y necesitás elegir la correcta según el tipo. Spring inyecta
TODAS las implementaciones como `List` y el registry las indexa UNA vez:

```java
@Component
public class SquareEffectRegistry {

    private final Map<SquareType, SquareEffectStrategy> strategies;

    // Spring inyecta TODAS las SquareEffectStrategy del contexto — agregar una nueva
    // implementación la registra SOLA, sin tocar esta clase.
    public SquareEffectRegistry(List<SquareEffectStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(SquareEffectStrategy::supports, Function.identity()));
    }

    public SquareEffectStrategy get(SquareType type) {
        SquareEffectStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("No strategy for square type: " + type);
        }
        return strategy;
    }
}

// Uso en el service — cero switch:
squareEffectRegistry.get(square.getType()).apply(game, player);
```

---

## 4. Factory — construir el tablero

**Cuándo**: crear objetos requiere lógica según tipo o configuración (armar el tablero del
enunciado: 30 casillas, cada 5 una especial, la última es meta).

```java
@Component
public class BoardFactory {

    /** Crea las casillas del tablero según las reglas del enunciado. */
    public List<Square> createBoard(Game game, int size) {
        List<Square> squares = new ArrayList<>();
        for (int position = 1; position <= size; position++) {
            squares.add(createSquare(game, position, size));
        }
        return squares;
    }

    private Square createSquare(Game game, int position, int size) {
        SquareType type;
        if (position == size) {
            type = SquareType.GOAL;
        } else if (position % 5 == 0) {
            type = SquareType.DAMAGE;               // las reglas EXACTAS las da el enunciado
        } else {
            type = SquareType.NORMAL;
        }
        Square square = new Square();
        square.setGame(game);
        square.setPosition(position);
        square.setType(type);
        return square;
    }
}
```

El service llama `boardFactory.createBoard(game, 30)` al crear la partida y persiste todo
junto (cascade desde `Game`). La lógica de construcción queda en UN lugar testeable.

---

## 5. Chain of Responsibility — validar un movimiento

**Cuándo**: una acción debe pasar varias validaciones/efectos EN ORDEN, y cada eslabón decide
cortar (excepción) o dejar pasar. Clásico: validar un movimiento.

```java
public interface MoveValidator {
    void validate(Game game, Player player, MoveRequest request);   // lanza excepción o deja pasar
}

@Component
@Order(1)                                           // el orden de la cadena es EXPLÍCITO
public class TurnValidator implements MoveValidator {
    @Override
    public void validate(Game game, Player player, MoveRequest request) {
        if (!game.getCurrentPlayerId().equals(player.getId())) {
            throw new NotYourTurnException("It is not player " + player.getId() + "'s turn");
        }
    }
}

@Component
@Order(2)
public class GameInProgressValidator implements MoveValidator {
    @Override
    public void validate(Game game, Player player, MoveRequest request) {
        if (game.getStatus() != GameStatus.IN_PROGRESS) {
            throw new InvalidGameStateException("Game is not in progress");
        }
    }
}

// Service — Spring inyecta la lista YA ordenada por @Order:
private final List<MoveValidator> moveValidators;

public GameDTO move(Long gameId, Long userId, MoveRequest request) {
    // ... cargar game y player ...
    moveValidators.forEach(validator -> validator.validate(game, player, request));
    // ... si ninguno lanzó, ejecutar el movimiento ...
}
```

**Diferencia con Strategy**: Strategy elige UNA variante y la ejecuta; Chain ejecuta TODAS
en orden y cualquiera puede cortar.

---

## 6. El resto de GoF — referencia rápida (cuándo PODRÍAN aparecer)

| Patrón | En el parcial aparece como... | ¿Escribirlo a mano? |
|---|---|---|
| **Singleton** | Los beans de Spring (`@Component`/`@Service`) YA son singletons | NO — lo da Spring |
| **Builder** | `@Builder` de Lombok en DTOs | NO — lo da Lombok |
| **Template Method** | Flujo fijo con pasos que varían (turno: tirar → mover → aplicar casilla → chequear victoria) — clase abstracta con el esqueleto | Solo si hay 2+ variantes del flujo |
| **Observer** | Eventos de dominio (`ApplicationEventPublisher`) — "cuando alguien gana, registrar X" | Raro en un parcial; un llamado directo alcanza |
| **Adapter** | El client de una API externa que traduce su JSON a tu dominio | Sí, si hay API externa |
| **Facade** | El service que orquesta varios repos/mappers YA es una fachada | NO — es la arquitectura |
| **Decorator / Proxy / Composite / Bridge / Flyweight / Prototype / Memento / Visitor / Mediator / Interpreter / Iterator / Command** | Casi nunca en este parcial | NO agregar por las dudas |

**Si te piden "usar un patrón" y dudás**: State para fases, Strategy+Registry para tipos,
Factory para el tablero, Chain para validaciones. Con esos cinco cubrís el 95% de los
enunciados de juegos.

---

