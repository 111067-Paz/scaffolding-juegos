# Adaptar el juego base a un enunciado

> Referencia de la skill game-engine. El juego base (carrera de dados) ya resuelve lo que se
> repite: partida con fases, jugadores con turno, tablero, azar, vidas, victoria, historial,
> ownership y concurrencia. Adaptar = renombrar/reemplazar piezas, no empezar de cero.

## Qué se queda casi siempre

| Pieza | Por qué se queda |
|---|---|
| `Game` + `GameStatus` + `GameState`/`GameStateRegistry` | Toda partida tiene fases |
| `Player` (turnOrder, lives) | Todo juego por turnos tiene participantes con orden |
| `Move` (historial inmutable) | "Mostrar las jugadas" es un requisito casi fijo |
| `MoveValidator` chain | Siempre hay "¿es tu turno?", "¿la jugada es legal?" |
| `DiceRoller` (o `RandomProvider`) | Todo azar detrás de una interfaz → tests deterministas |
| Auth + ownership + `@Version` | Infraestructura; no depende del juego |

## Qué se reemplaza según el juego

| Juego del enunciado | Tablero / Cell | Strategy (qué varía) | Validadores extra | Victoria |
|---|---|---|---|---|
| Serpientes y escaleras / Ludo / Oca | Igual (`Cell` con destino) | `SnakeCell`, `LadderCell` (mover a `targetPosition`) | — | llegar a la meta (exacto o no) |
| Batalla naval | `Cell` con `row`, `col`, `ship` | tipos de disparo / de barco | celda no disparada antes, dentro del tablero | hundir toda la flota |
| Ta-te-ti / 4 en línea | `Cell` con `row`, `col`, `owner` | — (2 casos estables: no hace falta) | celda libre, columna no llena | línea de N (algoritmo tradicional) |
| UNO / cartas | `Card` (mazo, mano, pila) en vez de `Cell` | efecto por tipo de carta (`SkipCard`, `ReverseCard`, `DrawTwoCard`) | carta jugable sobre la pila | mano vacía |
| TCG / Pokémon | `Card` + `ActivePokemon` | efectos de carta/ataque | energía suficiente, fase correcta | premios / sin cartas |
| Trivia / Preguntados | `Question` + `Category` | tipo de pregunta o categoría | tiempo, pregunta no respondida | puntaje / categorías completas |
| Memotest | `Card` con `pairId`, `revealed` | — | carta no revelada, máx. 2 por turno | todos los pares |

**Cómo decidir:** si el enunciado lista TIPOS de algo (casillas, cartas, preguntas) con
comportamiento distinto → Strategy + Registry. Si solo hay 2 casos estables → `if`.

## Pasos (en este orden)

1. `ENUNCIADO.md`: pegar la consigna cruda. `/arranque` propone el modelo.
2. Entidades: renombrar/agregar (skill `jpa-modelado`). Mantener `Game`/`Player`/`Move`.
3. `CellType` → los tipos del enunciado; una `CellEffect` por tipo; borrar las que no aplican
   (el registry se arma solo desde los beans).
4. `BoardFactory.resolveType` → reglas de armado del enunciado (o `DeckFactory`, `QuestionFactory`).
5. `TurnEngine.play/findWinner/advanceTurn` → reglas de turno y victoria.
6. Validadores nuevos → una clase `@Component @Order(n)` por regla.
7. Tests: copiar el test hermano (`CellEffectsTest`, `TurnEngineTest`, `GameFlowIntegrationTest`)
   y ajustar los valores. El `DiceRoller` mockeado hace el flujo determinista.
8. FE: adaptar `game.models.ts` (espejo del JSON), `CELL_META`, `board-grid`.

## Información oculta (cartas, batalla naval)

El servidor **nunca** manda el estado completo a todos: proyectar vistas por jugador
(`PlayerViewDTO` con su mano; `OpponentViewDTO` con cantidad de cartas, sin contenido;
batalla naval: disparos del rival sí, posiciones de sus barcos no). La decisión se toma en el
mapper/service, nunca ocultando con CSS en el front.

## Tiempo real (solo si el enunciado lo pide)

Por turnos → REST alcanza (el juego base). Varios jugadores en navegadores distintos que
tienen que ver la jugada del otro → polling simple con `httpResource` + `reload()` cada N
segundos, o WebSocket/STOMP si la cátedra lo exige. Nunca WebSocket "porque suena pro".
