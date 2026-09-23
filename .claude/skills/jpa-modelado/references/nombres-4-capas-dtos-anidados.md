# Nombres en las 4 capas y DTOs anidados

> Referencia de la skill jpa-modelado.

## 5. Convenciones de nombres en las 4 capas

Una misma propiedad atraviesa 4 mundos con 4 convenciones. El mapeo de punta a punta:

| Capa | Convención | Ejemplo | Cómo se logra |
|------|-----------|---------|----------------|
| Java (campo) | `camelCase` | `fechaInicio` | — |
| Columna BD | `snake_case` | `fecha_inicio` | naming strategy default de Hibernate, o `@Column(name = ...)` explícito |
| Tabla BD | `snake_case` (plural) | `turnos`, `deck_cards` | `@Table(name = "...")` |
| JSON (contrato API) | lo que diga el contrato | `"nombre_completo"` | `@JsonProperty("...")` en el DTO |
| TypeScript (FE) | `camelCase` | `nombreCompleto` | interfaz del modelo (matchea el JSON, ver nota) |

### Columna BD

```java
// La naming strategy default convierte fechaInicio → fecha_inicio (minúsculas) AUTOMÁTICAMENTE.
private LocalDateTime fechaInicio;            // columna: fecha_inicio

// Para un nombre EXACTO (mayúsculas, abreviaturas, legacy) → @Column explícito:
@Column(name = "Fecha_Inicio")                // la default NUNCA pondría mayúscula
private LocalDateTime fechaInicio;
```

### JSON — `@JsonProperty` respeta el contrato (caso real del clinic)

```java
public class PacienteDTO {
    @JsonProperty("nombre_completo")          // el contrato exige snake_case en el JSON
    private String nombreCompleto;            // el campo Java sigue camelCase

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaHora;          // sin @JsonProperty → queda "fechaHora" (camelCase)
}
```

**Por qué importa**: la cátedra exige "respetar estrictamente el contrato". Un typo en una key
(`nombrecompleto` vs `nombre_completo`) rompe el contrato y pierde puntos. El compilador NO te
avisa — `@JsonProperty` es la única garantía.

### Nota TS — el JSON manda en ambos lados

Si el BE serializa `"nombre_completo"`, la interfaz TS debe leer exactamente esa key:

```typescript
export interface Paciente {
  id: number;
  nombre_completo: string;   // matchea el JSON del contrato
}
```
Si querés `camelCase` puro en TS, mapeás en el service (`map(p => ({ nombreCompleto: p.nombre_completo }))`).
Para el parcial: matchear el contrato es lo más seguro y rápido.

---

## 6. Exponer relaciones sin recursión — DTOs anidados (lo recomendado en 2026)

NUNCA serialices una entity con relaciones directamente: una relación bidireccional
(Deck→DeckCard→Deck) entra en **recursión infinita** y revienta el JSON. La solución profesional
y recomendada en 2026: **DTOs**, con request y response separados.

### ✅ BIEN — DTOs anidados, request ≠ response (patrón del clinic)

```java
// RESPONSE: anida DTOs hijos, NO entities. Sin ciclos.
public class TurnoDTO {
    private Long id;
    private PacienteDTO paciente;          // DTO, no PacienteEntity
    private EstudioDTO estudio;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaHora;
}

// REQUEST: solo los ids de las relaciones, no objetos completos
public class NewTurnoDTO {
    @JsonProperty("paciente_id") @NotNull private Long pacienteId;
    @JsonProperty("estudio_id")  @NotNull private Long estudioId;
}
```

El service resuelve los ids del request a entities (buscándolas en su repo) y arma el DTO de
response con los hijos ya cargados.

**Alternativa legacy (mencionada, no recomendada)**: `@JsonManagedReference`/`@JsonBackReference`
o `@JsonIgnore` sobre la entity. Sirve para cortar el ciclo, pero expone entities y acopla el
JSON al modelo. Usá DTOs.

---
