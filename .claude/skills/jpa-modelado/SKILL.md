---
name: jpa-modelado
description: >
  Modelado JPA para el parcial: entidades con Lombok correcto (nunca @Data), relaciones
  (@ManyToOne/@OneToMany/@ManyToMany con join-entity/@OneToOne), LAZY siempre, cascade según
  composición vs asociación, @ForeignKey nombrado, @Version para concurrencia, nombres en las 4
  capas, DTOs anidados sin recursión y N+1 resuelto con @Query + JOIN FETCH (nunca
  @EntityGraph ni EAGER). El estado del juego SIEMPRE persiste en entidades.
  Trigger: al crear o editar entidades, relaciones, columnas, claves foráneas, queries de
  repository, o al modelar el dominio de un juego (partida, jugador, tablero, casillas,
  movimientos) aunque el usuario no diga "JPA".
license: Apache-2.0
metadata:
  author: PazLuciano
  version: "2.0"
---

# jpa-modelado

Ejemplo vivo: `entities/Game`, `Player`, `Cell`, `Move` + `repositories/GameRepository`.

## 1. Plantilla de entidad

```java
@Entity
@Table(name = "players")                       // snake_case plural
@Getter @Setter @NoArgsConstructor @AllArgsConstructor   // NUNCA @Data
public class Player {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)          // LAZY explícito
    @JoinColumn(name = "game_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_player_game")) // FK con nombre
    private Game game;

    @Column(name = "turn_order", nullable = false)
    private Integer turnOrder;
}
```

- `@Data` en entidades rompe `equals/hashCode/toString` con relaciones lazy → recursión y
  queries accidentales.
- Enums con `@Enumerated(EnumType.STRING)` (legible, no se rompe al reordenar).
- Dinero = `BigDecimal` + `@Column(precision = 12, scale = 2)`; nunca `double`.
- Fechas: `LocalDateTime` + `@PrePersist` (ver `Game.onCreate`).

## 2. Relaciones — decidir antes de escribir

| Relación | Dueño (`@JoinColumn`) | Inverso | Cascade |
|---|---|---|---|
| Partida → jugadores/casillas (**composición**: el hijo no existe sin el padre) | hijo `@ManyToOne` | padre `@OneToMany(mappedBy)` | `ALL` + `orphanRemoval = true` |
| Movimiento → jugador (**asociación**: referencia algo que vive solo) | `Move.player` | — (unidireccional) | ninguno |
| N–M con datos (inventario, participación) | join-entity con 2 `@ManyToOne` | 2 `@OneToMany` | según composición |

- `@ManyToOne` y `@OneToOne` son EAGER por defecto → **siempre** `fetch = LAZY`.
- Colecciones inicializadas (`= new ArrayList<>()`) y métodos helper que sincronizan los dos
  lados (`game.addPlayer(p)` setea `p.setGame(this)`).
- `@OrderBy("turnOrder ASC")` cuando el orden importa para la lógica.
- Detalle y casos reales: [references/cardinalidades-fetch-cascade.md](references/cardinalidades-fetch-cascade.md).

## 3. N+1 — `@Query` + `JOIN FETCH` (estándar del proyecto)

```java
@Query("select distinct g from Game g left join fetch g.players where g.id = :id")
Optional<Game> findWithPlayersById(@Param("id") Long id);
```

- **Dos colecciones `List` en el mismo JOIN FETCH → `MultipleBagFetchException`.** Solución del
  scaffold: dos queries en la MISMA transacción (`findWithPlayersById` + `findWithCellsById`);
  la segunda completa la instancia ya gestionada.
- Listados: traer solo lo que muestra el DTO (`findSummariesByOwnerId` hace `join fetch g.winner`).
- Si el DTO toca una relación lazy fuera del JOIN FETCH → N+1 (o `LazyInitializationException`
  con `open-in-view=false`). El mapper asume que el service ya cargó todo.
- PROHIBIDO `@EntityGraph` (penalizado) y "arreglar" con `EAGER`.

## 4. Concurrencia — `@Version`

```java
@Version
private Long version;   // en la raíz del agregado (Game)
```

Dos requests que modifican la misma partida → el segundo commit falla con
`ObjectOptimisticLockingFailureException` → 409 en el handler. Para que cada turno incremente
la versión, la fila del agregado tiene que cambiar (`updatedAt` con `@PreUpdate`).

## 5. El estado del juego vive en la base

- Partida, turnos, posiciones, vidas, tablero e historial = **entidades**. PROHIBIDO un
  `Map` estático o un singleton con estado: reiniciar la app (o el contenedor) no puede
  perder partidas. En Docker, el perfil `docker` usa PostgreSQL con `ddl-auto=update`.
- Historial = entidad inmutable (`Move`) consultada por id de partida, no una colección
  gigante en el padre.

## 6. Nombres en las 4 capas

| Capa | Convención | Ejemplo |
|---|---|---|
| Java | camelCase | `boardSize` |
| BD | snake_case (naming strategy; `@Column(name)` solo si difiere) | `board_size` |
| JSON | EXACTO al contrato → `@JsonProperty("board_size")` en el DTO | `"board_size"` |
| TS | nombres del JSON en las interfaces de respuesta | `board_size: number` |

Exponer relaciones = **DTOs anidados** (`GameDTO.players: List<PlayerDTO>`), nunca la entidad.
Más: [references/nombres-4-capas-dtos-anidados.md](references/nombres-4-capas-dtos-anidados.md).

## Checklist

- [ ] `@Getter @Setter @NoArgsConstructor @AllArgsConstructor`, sin `@Data`
- [ ] Todo `@ManyToOne`/`@OneToOne` con `LAZY`; `@ForeignKey(name = "fk_...")`
- [ ] `mappedBy` en el inverso; cascade solo en composición
- [ ] Queries de detalle con JOIN FETCH; nunca dos bags en la misma query
- [ ] `@Version` en la entidad que se modifica concurrentemente
- [ ] Ningún estado de juego en memoria
