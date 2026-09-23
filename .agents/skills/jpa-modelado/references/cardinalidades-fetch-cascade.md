# Cardinalidades, fetch y cascade

> Referencia de la skill jpa-modelado (casos reales de proyectos anteriores). El ejemplo vivo del scaffold es Game → Player/Cell (composición, cascade ALL + orphanRemoval) y Move (asociación, sin cascade).

## 2. Las cardinalidades

### @ManyToOne + @OneToMany (la más común — relación 1–N)

`@ManyToOne` va en el lado **dueño** (el que tiene la FK). `@OneToMany` es el lado **inverso**
(`mappedBy` apunta al campo dueño). Caso real del proyecto:

```java
// Lado DUEÑO (tiene la columna FK owner_id)
@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "owner_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_decks_owner"))
private User owner;

// Lado INVERSO (no tiene columna; mappedBy = nombre del campo dueño en DeckCard)
@OneToMany(mappedBy = "deck", cascade = CascadeType.ALL,
        orphanRemoval = true, fetch = FetchType.LAZY)
private List<DeckCard> cards = new ArrayList<>();   // SIEMPRE inicializar la colección
```

Reglas:
- `mappedBy` va en el lado **inverso** y nombra el campo del lado dueño. Sin `mappedBy`, JPA crea
  una tabla intermedia o columna extra por error (relación duplicada).
- Inicializá las colecciones (`= new ArrayList<>()`) para evitar `NullPointerException`.
- Mantené ambos lados sincronizados con un helper (`addCard` setea `this` en el hijo).

### @ManyToMany — preferí join-entity explícita

```java
// ❌ EVITAR @ManyToMany puro cuando la relación tiene (o tendrá) atributos propios:
@ManyToMany
@JoinTable(name = "deck_cards",
        joinColumns = @JoinColumn(name = "deck_id"),
        inverseJoinColumns = @JoinColumn(name = "card_id"))
private List<Card> cards;          // no hay dónde guardar la cantidad, ni unicidad

// ✅ BIEN — join-entity explícita (caso real: DeckCard con quantity)
@Entity
@Table(name = "deck_cards",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_deck_cards_deck_card", columnNames = {"deck_id", "card_api_id"}))
@Getter @NoArgsConstructor
public class DeckCard {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "deck_id", nullable = false)
    private Deck deck;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id")
    private Card card;

    @Column(nullable = false)
    private int quantity;          // ← el atributo que un @ManyToMany puro no podría guardar
}
```

**Regla 2026**: `@ManyToMany` puro solo si la relación NO tiene ni tendrá atributos. En cuanto
aparece una `cantidad`, una `fecha`, un `precio` → join-entity explícita (dos `@ManyToOne`).
Migrar de `@ManyToMany` a join-entity después es costoso; arrancá con la entity si hay duda.

### @OneToOne

```java
@OneToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "user_id", unique = true)
private User user;
```

---

## 3. `fetch` — LAZY por defecto, SIEMPRE

| Relación | Default de JPA | Qué usar |
|----------|----------------|----------|
| `@ManyToOne` | **EAGER** | forzar `fetch = LAZY` |
| `@OneToOne` | **EAGER** | forzar `fetch = LAZY` |
| `@OneToMany` | LAZY | LAZY (ok) |
| `@ManyToMany` | LAZY | LAZY (ok) |

**`@ManyToOne` y `@OneToOne` son EAGER por defecto** — esto trae el objeto relacionado en CADA
query aunque no lo uses, y es una causa silenciosa de N+1. Ponelos `LAZY` explícito siempre.
Cuando necesités el dato relacionado, lo traés con `JOIN FETCH` (sección 6), no con EAGER.

---

## 4. `cascade` — analizar los 6 tipos y aplicar el correcto

`cascade` define qué operaciones del padre se propagan a los hijos.

| CascadeType | Propaga al hijo cuando... | Usar cuando |
|-------------|---------------------------|-------------|
| `PERSIST` | guardás el padre | el hijo es nuevo y se crea junto al padre |
| `MERGE` | actualizás el padre | querés que los cambios bajen a los hijos |
| `REMOVE` | borrás el padre | **composición**: el hijo no existe sin el padre |
| `REFRESH` | recargás el padre desde BD | raro; recargar el grafo completo |
| `DETACH` | desasociás el padre del contexto | raro |
| `ALL` | todas las anteriores | composición clara (padre dueño exclusivo) |

Más `orphanRemoval = true`: borra el hijo cuando lo **sacás de la colección** del padre
(distinto de `REMOVE`, que actúa al borrar el padre).

### ✅ Composición — el hijo pertenece al padre (Deck → DeckCard)

```java
@OneToMany(mappedBy = "deck", cascade = CascadeType.ALL, orphanRemoval = true)
private List<DeckCard> cards = new ArrayList<>();
```
Borrar el deck borra sus `DeckCard`; sacar un `DeckCard` de la lista lo elimina de la BD. Correcto:
un `DeckCard` no tiene sentido sin su deck.

### ❌ Asociación — NUNCA cascade hacia algo compartido

```java
// Turno → Paciente: el paciente EXISTE independientemente del turno
@ManyToOne(fetch = FetchType.LAZY, optional = false)
// ❌ cascade = CascadeType.ALL  →  borrar un turno borraría al paciente (¡y sus otros turnos!)
@JoinColumn(name = "paciente_id")
private Paciente paciente;          // SIN cascade
```

**Regla**: `cascade`/`orphanRemoval` SOLO en relaciones de **composición** (el hijo es parte del
padre). En **asociaciones** a entidades compartidas (un paciente, un usuario, un estudio) → sin
cascade. Un `cascade = REMOVE` mal puesto borra datos de otros.

---
