# Límites, clases de equivalencia y caminos de error

> Referencia de la skill java-testing. En el scaffold: BoardFactoryTest (@ParameterizedTest), CellEffectsTest (límites 0/meta), GameControllerTest (400/404/409).

## 2. Probar límites y clases de equivalencia

Por cada condición del método bajo prueba, derivar los tests de sus LÍMITES:

| Condición en el código | Tests obligatorios |
|------------------------|--------------------|
| `if (x > 0)` | x = 0 (límite, falso), x = 1 (límite, verdadero), x = -1 |
| `damage = Math.max(0, damage)` | damage que queda en 0 exacto, negativo que se clampea, positivo |
| `@Size(max = 50)` | string de 50 (pasa), de 51 (falla), vacío, null |
| colección | vacía, un elemento, muchos |
| `weakness × 2` luego `resistance − 20` | con/sin weakness, con/sin resistance, AMBAS (orden importa) |

### ✅ BIEN — límites del DamageCalculator (caso real del proyecto)

```java
@Tag("unit")
@DisplayName("DamageCalculator")
class DamageCalculatorTest {

    private final DamageCalculator calculator = new DamageCalculator();

    @Test
    void calculate_whenWeaknessMatches_doublesDamage() { /* 30 → 60 */ }

    @Test
    void calculate_whenResistanceMatches_subtracts20() { /* 30 → 10 */ }

    @Test
    void calculate_whenWeaknessAndResistance_appliesWeaknessBeforeResistance() {
        // 30 → ×2 = 60 → −20 = 40. Si el orden estuviera invertido daría (30−20)×2 = 20.
        // Este test FIJA el orden del algoritmo: es el test más valioso de la clase.
    }

    @Test
    void calculate_whenResistanceExceedsDamage_clampsToZero() { /* 10 → −10 → 0 */ }

    @Test
    void calculate_whenDamageIsExactlyZero_appliesNoCounters() { /* límite exacto */ }

    @Test
    void calculate_whenWeaknessIsNull_keepsBaseDamage() { /* camino del null */ }
}
```

**Probá las líneas, no el método**: cada `if`, cada operador, cada `null`-check del método debe
tener un test que lo atraviese por verdadero Y por falso. Eso es lo que JaCoCo mide como
branch coverage.

### Parameterized tests para tablas de límites

```java
@ParameterizedTest(name = "base={0}, weakness={1} → expected={2}")
@CsvSource({
    "30, true,  60",
    "30, false, 30",
    "10, false, 10",
    "0,  true,  0"
})
void calculate_appliesWeaknessRule(int base, boolean hasWeakness, int expected) { ... }
```

Mismo método, todos los límites, cero duplicación.

---

## 3. Caminos de error — SIEMPRE testearlos

Un método con `orElseThrow` tiene DOS comportamientos. Testear solo el feliz es testear la mitad.

### ❌ MAL — caso real: CardControllerTest solo probaba 200

El controller tiene un endpoint `GET /{id}` cuyo service lanza not-found… y no había ningún
test del 404.

### ✅ BIEN

```java
@Test
void getCard_whenCardDoesNotExist_returns404() throws Exception {
    // GIVEN
    when(cardCacheService.findById(CARD_ID))
            .thenThrow(new CardNotFoundException("Card not found: " + CARD_ID));

    // WHEN / THEN
    mockMvc.perform(get("/api/cards/{id}", CARD_ID))
            .andExpect(status().isNotFound());
}

// En unitarios puros:
@Test
void findById_whenCardDoesNotExist_throwsCardNotFoundException() {
    // GIVEN
    when(cardRepository.findById(CARD_ID)).thenReturn(Optional.empty());

    // WHEN + THEN
    CardNotFoundException exception = assertThrows(
            CardNotFoundException.class,
            () -> service.findById(CARD_ID));
    assertTrue(exception.getMessage().contains(CARD_ID.toString()));
}
```

`assertThrows` devuelve la excepción: verificar también el MENSAJE, no solo el tipo.

---
