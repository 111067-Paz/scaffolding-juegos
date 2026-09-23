# Mockito y métodos privados

> Referencia de la skill java-testing.

## 4. Mockito — aislar exactamente la unidad

- `@ExtendWith(MockitoExtension.class)` + `@Mock` + `@InjectMocks` para unitarios puros (sin Spring).
- **Mockear TODAS las dependencias inyectadas — incluidos los mappers** (`@Mock private GameMapper gameMapper`).
  Para que esto sea posible, el service recibe el mapper por constructor — `new GameMapper()`
  adentro del service está PROHIBIDO: no se puede aislar en el test.
- **`when(...)` solo de lo que el test usa** — MockitoExtension falla con stubs innecesarios (bien).
- **`verify(...)` para interacciones**: que el service llamó al repository con el argumento correcto.
- **`ArgumentCaptor` cuando el argumento se construye adentro** (caso real del proyecto):

```java
ArgumentCaptor<CardSearchRequest> captor = ArgumentCaptor.forClass(CardSearchRequest.class);
verify(cardCacheService).findAll(captor.capture());
assertEquals("xy1", captor.getValue().getSetCode());
```

- **`@Spy`** cuando necesitás el objeto REAL pero interceptando un método puntual:

```java
@Spy
private CoinFlipService coinFlipService;
// ...
doReturn(CoinSide.HEADS).when(coinFlipService).flip();  // doReturn con spy, NO when().thenReturn()
```

Con spies usar SIEMPRE `doReturn/doThrow` — `when(spy.flip())` EJECUTA el método real al stubear.

---

## 5. Métodos privados — Reflection

Primero preguntate: ¿el método privado se puede cubrir a través del público? Si sí, preferilo.
Si la lógica privada es compleja y necesita tests directos:

**Test unitario puro → `ReflectionSupport` (JUnit Platform):**

```java
import org.junit.platform.commons.support.ReflectionSupport;

@Test
void resolveValidationCode_whenUnknownCode_returnsGenericError() {
    GlobalExceptionHandler handler = new GlobalExceptionHandler();

    Method method = ReflectionSupport.findMethod(
            GlobalExceptionHandler.class, "resolveValidationCode", String.class)
        .orElseThrow();

    String result = (String) ReflectionSupport.invokeMethod(method, handler, "SomethingNew");

    assertEquals("VALIDATION_ERROR", result);
}
```

**Test con Spring ya presente → `ReflectionTestUtils` (más directo):**

```java
import org.springframework.test.util.ReflectionTestUtils;

// invocar privado
String result = ReflectionTestUtils.invokeMethod(handler, "resolveValidationCode", "NotBlank");

// setear un campo privado sin setter (ej: @Value)
ReflectionTestUtils.setField(jwtService, "secretKey", "test-secret");
```

---
