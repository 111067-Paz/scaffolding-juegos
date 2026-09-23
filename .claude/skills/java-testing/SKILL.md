---
name: java-testing
description: >
  Testing Java para el parcial (fusión de junit5-testing-limites + java-testing-PazLuciano):
  JUnit 5 + Mockito + Spring Boot 4 Test, naming methodName_scenario_expected, @DisplayName y
  @Tag obligatorios, AAA (GIVEN/WHEN/THEN), límites y clases de equivalencia con
  @ParameterizedTest, caminos de error siempre, unitario puro vs slice web vs integración,
  @MockitoBean (Boot 4), azar determinista, y JaCoCo >= 95% que rompe el build.
  Trigger: SIEMPRE que se cree, revise o corrija un test Java, se trabaje bajo src/test/java,
  se configure JUnit/Mockito/JaCoCo o se mencione cobertura, coverage o el 95%.
license: Apache-2.0
metadata:
  author: PazLuciano
  version: "2.0"
---

# java-testing

`./mvnw -f BE/pom.xml verify` corre todos los tests + reporte + **`jacoco:check` ≥ 95% de
líneas**: por debajo, el build falla. Reporte: `BE/target/site/jacoco/index.html`.

## 1. Esqueleto obligatorio

```java
@ExtendWith(MockitoExtension.class)
@Tag("unit")                                   // "unit" | "integration"
@DisplayName("GameServiceImpl")
class GameServiceImplTest {

    @Mock private GameRepository gameRepository;       // TODAS las dependencias mockeadas
    @Mock private GameMapper gameMapper;                // incluidos los mappers
    @InjectMocks private GameServiceImpl gameService;

    @Test
    @DisplayName("findById of someone else's game answers NotFound (does not leak the id)")
    void findById_whenNotOwner_throwsNotFound() {
        // GIVEN
        when(gameRepository.findWithPlayersById(GAME_ID)).thenReturn(Optional.of(game));
        // WHEN + THEN
        assertThrows(GameNotFoundException.class, () -> gameService.findById(GAME_ID, OTHER_USER_ID));
        verify(gameRepository, never()).findWithCellsById(GAME_ID);
    }
}
```

- Naming `methodName_scenario_expected`; `@DisplayName` legible; `@Tag`.
- Un comportamiento por test. Datos repetidos → fixture compartido (`support/GameFixtures`).
- Testear **cada línea y cada rama** del método: camino feliz + cada `throw` + cada límite.

## 2. Qué tipo de test para qué

| Qué | Cómo | Ejemplo en el scaffold |
|---|---|---|
| Strategy / State / Validator / Factory / Engine | Unitario puro: `new` la clase (sin estado) o `@InjectMocks` | `CellEffectsTest`, `GameStatesTest`, `TurnEngineTest` |
| Service | `MockitoExtension` + `@Mock` de todo | `GameServiceImplTest` |
| Controller | `MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(new GlobalExceptionHandler())` + `.requestAttr(USER_ID_ATTRIBUTE, id)` | `GameControllerTest` |
| Mapper | Unitario puro con entidades armadas a mano | `GameMapperTest` |
| Flujo completo HTTP + JPA + interceptor | `@SpringBootTest @AutoConfigureMockMvc @Transactional @TestConstructor(autowireMode = ALL)` | `GameFlowIntegrationTest` |
| Cliente de API externa | `MockRestServiceServer.bindTo(RestClient.builder())` — nunca la API real | `ExternalApiClientTest` |

Boot 4: `@MockitoBean` (no `@MockBean`); `AutoConfigureMockMvc` está en
`org.springframework.boot.webmvc.test.autoconfigure`. **PROHIBIDO `@Autowired`** también en
tests: constructor + `@TestConstructor`. Detalle: [references/spring-tests.md](references/spring-tests.md).

## 3. Límites y clases de equivalencia

```java
@ParameterizedTest(name = "position {0} of 30 → {1}")
@CsvSource({ "0, NORMAL", "29, GOAL", "14, TRAP", "10, BACK", "12, LOSE_TURN", "8, ADVANCE" })
void resolveType_eachRule_returnsExpectedType(int position, CellType expected) { ... }
```

Siempre probar: 0, 1, máximo, máximo+1, negativos, null/vacío, "justo en la meta", "se pasa
de la meta", "vida 1 → 0", "último jugador vivo". Casos reales:
[references/limites-y-errores.md](references/limites-y-errores.md).

## 4. Caminos de error — SIEMPRE

Por cada endpoint: el feliz + 400 (validación / body malformado) + 404 (no existe o ajeno) +
409 (fase o turno incorrecto) + 401 donde aplique. Por cada service: cada `orElseThrow` y cada
regla que lanza. Verificar también lo que **no** pasó (`verify(repo, never()).save(any())`).

## 5. Juegos: azar y orden

- El azar se inyecta (`DiceRoller`): en unitarios se pasa el valor; en integración
  `@MockitoBean DiceRoller` + `when(diceRoller.roll()).thenReturn(3)` → partida reproducible.
- Validar el orden de la chain con `InOrder` y que un link fallido corta la cadena
  (`verify(next, never())`).
- `RandomDiceRoller`: `@RepeatedTest` para acotar el rango (1..6).

## 6. Mockito y privados

- `when(...)` solo de lo que se usa (MockitoExtension estricto falla con stubs de más).
- `ArgumentCaptor` cuando el objeto se construye adentro (ver `create_withValidRequest_...`).
- Métodos privados: testear a través del público; si no queda otra, `ReflectionSupport`.
- Referencias: [references/mockito-referencia.md](references/mockito-referencia.md),
  [references/mockito-y-privados.md](references/mockito-y-privados.md).

## 7. JaCoCo

- Regla en el `pom.xml`: BUNDLE / LINE / COVEREDRATIO ≥ 0.95 en la fase `verify`.
- Excluido solo `P4Application` (bootstrap). DTOs/entidades Lombok no cuentan
  (`lombok.addLombokGeneratedAnnotation = true`).
- ¿No llega? Mirar el reporte HTML por clase: casi siempre falta un camino de error o un límite.
  **Nunca** agregar excludes para "llegar".

## Checklist

- [ ] `@Tag`, `@DisplayName`, naming `method_scenario_expected`, GIVEN/WHEN/THEN
- [ ] Todas las dependencias mockeadas en unitarios (mappers incluidos)
- [ ] Cada rama y cada `throw` cubiertos; límites con `@ParameterizedTest`
- [ ] Controller slice con el `GlobalExceptionHandler` registrado
- [ ] Integración con `@TestConstructor`, sin `@Autowired`, azar mockeado
- [ ] `./mvnw verify` en verde con `All coverage checks have been met`
