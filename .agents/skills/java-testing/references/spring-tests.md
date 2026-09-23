# Unitario vs slice vs integración (Spring Boot 4)

> Referencia de la skill java-testing. En el scaffold: GameFlowIntegrationTest (@SpringBootTest + @MockitoBean DiceRoller) y AuthJwtIntegrationTest.

## 6. Unitario vs slice vs integración

| Tipo | Herramienta | Cuándo | Tag |
|------|------------|--------|-----|
| Unitario puro | `MockitoExtension`, sin Spring | services, mappers, calculators, validators | `@Tag("unit")` |
| Slice web | `MockMvcBuilders.standaloneSetup(controller)` o `@WebMvcTest` | controllers (status, JSON, validación) | `@Tag("unit")` |
| Slice JPA | `@DataJpaTest` (H2 automático) | queries custom, specifications | `@Tag("integration")` |
| Integración | `@SpringBootTest` | flujo completo controller→service→repo | `@Tag("integration")` |

Reglas:
- La pirámide manda: MUCHOS unitarios, pocos de integración.
- Integración con H2 — nunca contra base externa.
- API externa SIEMPRE mockeada. En unitarios `@Mock` del cliente; en tests con contexto Spring
  usar **`@MockitoBean`** (`org.springframework.test.context.bean.override.mockito`) —
  **`@MockBean` fue ELIMINADO en Boot 4**, no compila.
- **Flujo completo obligatorio**: al menos un `@SpringBootTest` que recorra el caso de uso
  principal de punta a punta (crear → consultar → modificar → verificar estado).
- **Sin `@Autowired` tampoco en tests** — la regla del proyecto vale también acá. En un
  `@SpringBootTest` se inyecta por constructor con `@TestConstructor(autowireMode = ALL)`,
  igual que el código de producción.

### ✅ BIEN — inyección por constructor en `@SpringBootTest` (sin `@Autowired`)

```java
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Tag("integration")
class ProductIntegrationTest {

    private final MockMvc mockMvc;

    ProductIntegrationTest(MockMvc mockMvc) {   // Spring autowirea cada parámetro
        this.mockMvc = mockMvc;
    }
}
```

### ❌ MAL — field injection con `@Autowired`

```java
@SpringBootTest
class ProductIntegrationTest {
    @Autowired                       // ❌ prohibido por la regla del proyecto, también en tests
    private MockMvc mockMvc;
}
```

> Nota Boot 4 — cambios verificados contra los jars 4.0.0:
> - `@AutoConfigureMockMvc` y `@WebMvcTest` viven en `org.springframework.boot.webmvc.test.autoconfigure`
>   (no en `...test.autoconfigure.web.servlet`).
> - `@MockBean` NO EXISTE más — usar `@MockitoBean` de
>   `org.springframework.test.context.bean.override.mockito`.

### El enfoque de la cátedra: `@SpringBootTest` + `@MockitoBean` / `@MockitoSpyBean`

La cátedra testea el service con **contexto Spring** y los colaboradores reemplazados por mocks
del contexto. NO es unitario puro con `MockitoExtension` — es el patrón que viste en el código
base del profe.

```java
@SpringBootTest
@Tag("integration")
@DisplayName("TurnoService — enfoque de cátedra")
class TurnoServiceTest {

    @MockitoSpyBean              // el PROPIO service como spy: ejecuta su lógica real,
    private TurnoService turnoService;   // pero permite stubear métodos internos puntuales

    @MockitoBean                 // colaboradores reemplazados por mocks DENTRO del contexto Spring
    private TurnoRepository turnoRepository;
    @MockitoBean
    private PacienteService pacienteService;   // ...y el resto de colaboradores

    @Test
    @DisplayName("createTurno lanza 400 cuando el paciente no existe")
    void createTurno_whenPacienteNoExiste_throwsBadRequest() {
        // GIVEN
        when(pacienteService.findById(1L)).thenThrow(new PacienteNotFoundException(1L));
        // WHEN + THEN
        assertThrows(PacienteNotFoundException.class,
                () -> turnoService.createTurno(request));
    }
}
```

| | Unitario puro | Enfoque cátedra |
|---|---|---|
| Anotaciones | `@ExtendWith(MockitoExtension.class)` + `@Mock`/`@InjectMocks` | `@SpringBootTest` + `@MockitoBean`/`@MockitoSpyBean` |
| Contexto Spring | NO (rápido) | SÍ (más lento) |
| Mockear colaborador | `@Mock` | `@MockitoBean` |
| Espiar el objeto bajo prueba | `@Spy` | `@MockitoSpyBean` |
| Cuándo | tu default por velocidad | cuando la cátedra lo pide o necesitás el contexto |

`@MockitoSpyBean` sobre el propio service = el equivalente Spring de `@Spy`: corre la lógica real
y solo stubeás lo que indiques (con `doReturn(...).when(spy).metodo()`).

---

## 7. JaCoCo

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.12</version>
    <executions>
        <execution><goals><goal>prepare-agent</goal></goals></execution>
        <execution><id>report</id><phase>prepare-package</phase>
            <goals><goal>report</goal></goals></execution>
    </executions>
</plugin>
```

- Reporte: `mvn clean test jacoco:report` → `target/site/jacoco/index.html`.
- **Cobertura mínima OBLIGATORIA ≥ 95% de line coverage (cobertura lineal)** — es el piso de
  aprobación de la cátedra, no la meta. La meta sigue siendo cubrir cada línea del método.
- Mirar TAMBIÉN **branch coverage**: un `if` cubierto solo por verdadero es una rama sin
  testear. Cubrir todas las ramas garantiza el 95% lineal de yapa.
- La cobertura se LOGRA testeando límites y errores (secciones 2 y 3) — nunca escribiendo
  tests sin asserts para "pintar líneas".

> Nota JDK 21: al correr los tests puede aparecer el warning "Mockito is currently self-attaching
> to enable the inline-mock-maker". Es solo un warning — NO rompe los tests ni la cobertura. La
> cátedra trabaja así, sin configuración extra; lo dejamos tal cual.

---
