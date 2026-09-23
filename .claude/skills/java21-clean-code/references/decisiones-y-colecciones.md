# Tradicional vs stream vs patrón, y colecciones

> Referencia de la skill java21-clean-code. En el scaffold: TurnEngine.advanceTurn (for con orden y efectos) vs GameMapper (streams puros).

## 2. Decisión: ¿tradicional, stream, algoritmo o patrón?

Antes de escribir un método, preguntate en este orden:

```
1. ¿Es una TRANSFORMACIÓN de colección (filtrar/mapear/agrupar/reducir)?
       → STREAM
2. ¿Es un ALGORITMO con pasos ordenados, estado mutable o cortes tempranos complejos?
       → TRADICIONAL (for / if secuencial)
3. ¿El if/switch crece cada vez que aparece un caso nuevo?
       → PATRÓN (Strategy / Chain / registry)
4. ¿Hay un problema de BÚSQUEDA/ORDENAMIENTO con costo relevante?
       → Elegir el ALGORITMO y la ESTRUCTURA correcta (ver sección 3)
```

### ✅ BIEN — tradicional para un algoritmo secuencial (caso real: DamageCalculator)

```java
public void calculate(AttackContext ctx) {
    int damage = ctx.getBaseDamage();              // 1. base
    if (defender.getWeakness() != null
            && defender.getWeakness().equalsIgnoreCase(attacker.getType())) {
        damage *= 2;                               // 3. weakness ×2
    }
    if (defender.getResistance() != null
            && defender.getResistance().equalsIgnoreCase(attacker.getType())) {
        damage -= 20;                              // 4. resistance −20
    }
    damage = Math.max(0, damage);                  // 6. mínimo 0 — AL FINAL
    ctx.setCalculatedDamage(damage);
}
```

**Por qué tradicional**: los pasos tienen ORDEN obligatorio y mutan un acumulador. Forzar un
stream acá oscurecería el algoritmo. La elección correcta es imperativa y legible.

### ✅ BIEN — stream para composición declarativa (caso real: CardSpecificationBuilder)

```java
List<Specification<Card>> activeSpecifications = FILTER_REGISTRY.stream()
        .map(filter -> filter.apply(request))
        .filter(Objects::nonNull)
        .toList();

return activeSpecifications.stream()
        .reduce(neutralSpecification, Specification::and);
```

**Por qué stream**: es map → filter → reduce puro, sin estado mutable ni orden de efectos.
Además el registry de funciones cumple OCP: agregar un filtro nuevo = agregar UNA línea a la
lista, sin tocar ningún `if`.

### ❌ MAL — stream forzado donde hay efectos y orden

```java
// ❌ forEach con efectos colaterales y dependencia de orden: es un for disfrazado
responses.stream().forEach(response -> {
    if (!repository.existsByApiId(response.getId())) {
        repository.save(mapper.toEntity(response));
        counter++;                                  // ❌ mutar estado externo en stream
    }
});
```

### ❌ MAL — if-else que crece por cada caso nuevo

```java
// ❌ cada filtro nuevo = otro if = modificar el método (viola OCP)
if (request.getSetCode() != null) { spec = spec.and(hasSetCode(...)); }
if (request.getName() != null)    { spec = spec.and(nameContains(...)); }
if (request.getSupertype() != null) { ... }
// → cuando esto crece, migrar al registry funcional o Strategy de arriba
```

---

## 3. Colecciones — elegir por operación dominante

| Necesito... | Estructura | Costo |
|-------------|-----------|-------|
| Orden de inserción, acceso por índice, duplicados | `ArrayList` | get O(1), contains **O(n)** |
| Saber si un elemento EXISTE (lookup masivo) | `HashSet` | contains **O(1)** |
| Clave → valor | `HashMap` | get/put O(1) |
| Iterar ordenado por clave natural | `TreeMap` / `TreeSet` | O(log n) |
| Cola FIFO / procesamiento por turnos | `ArrayDeque` | offer/poll O(1) |

**Regla práctica**: si vas a llamar `contains()` dentro de un loop, la colección DEBE ser un
`Set`. `List.contains()` dentro de un `for` es O(n²) escondido.

### ✅ BIEN — caso real: anti N+1 con Set

```java
// UNA query, y el lookup en el loop es O(1)
Set<String> existingApiIds = cardRepository.findBySetCode(setCode)
    .stream().map(Card::getApiId).collect(Collectors.toSet());

List<Card> newCards = responses.stream()
    .filter(response -> !existingApiIds.contains(response.getId()))  // O(1)
    .map(cardMapper::toEntity)
    .toList();
```

### ❌ MAL — O(n²) y N+1

```java
List<String> existingIds = new ArrayList<>();
for (Card card : cardRepository.findAll()) { existingIds.add(card.getApiId()); }
for (PokemonTcgCardResponse response : responses) {
    if (!existingIds.contains(response.getId())) {   // ❌ O(n) por iteración → O(n²)
        cardRepository.save(...);                     // ❌ un INSERT por elemento
    }
}
```

**Declarar por la interfaz**: `List<Card> cards`, `Set<String> ids`, `Map<UUID, Card> byId` —
nunca `ArrayList<Card> cards` en firmas o campos. Permite cambiar la implementación sin romper nada.

---

## 4. Nulls y Optional

- `Optional` es para RETORNOS de búsqueda que pueden no encontrar — nunca para campos ni parámetros.
- Encadenar sobre el Optional del repositorio en vez de `isPresent()` + `get()`:

### ✅ BIEN (caso real)

```java
return cardRepository.findById(id)
    .map(cardDtoMapper::toDTO)
    .orElseThrow(() -> new CardNotFoundException("Card not found: " + id));
```

### ❌ MAL

```java
Optional<Card> optionalCard = cardRepository.findById(id);
if (optionalCard.isPresent()) {            // ❌ desarma el Optional a mano
    return mapper.toDTO(optionalCard.get());
}
throw new CardNotFoundException(...);
```

- Validar argumentos al inicio del método (guard clauses) y fallar rápido con la excepción correcta.
- Nunca devolver `null` de un método que retorna colección — devolver `List.of()` (lista vacía).

---
