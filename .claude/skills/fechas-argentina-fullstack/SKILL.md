---
name: fechas-argentina-fullstack
description: >
  Fecha y hora de punta a punta (BD → Spring Boot → JSON → Angular) con formato declarado POR
  CAMPO (@JsonFormat "dd-MM-yyyy" / "dd-MM-yyyy HH:mm:ss"), sin configuración global
  (ni spring.jackson.time-zone, ni date-format, ni LOCALE_ID global). LocalDate/LocalDateTime,
  nunca java.util.Date. En Angular el string se muestra tal cual y se parsea con un helper
  explícito para operar; conversión para inputs type="date".
  Trigger: al declarar campos de fecha/hora en entidades o DTOs, serializar fechas con
  Jackson, mostrar/ordenar fechas en Angular o manejar inputs date/datetime-local.
license: Apache-2.0
metadata:
  author: PazLuciano
  version: "1.1"
---

# fechas-argentina-fullstack

## 1. Regla rectora: formato POR CAMPO, nunca global

- PROHIBIDO (salvo pedido explícito): `spring.jackson.time-zone`, `spring.jackson.date-format`,
  `LOCALE_ID` global en `app.config.ts`.
- **`spring.jackson.time-zone` es INERTE sobre `LocalDateTime`/`LocalDate`**: solo afecta a
  `java.util.Date`/`Calendar`. Ponerla da una falsa sensación de control.
- Cada campo declara su formato con `@JsonFormat`. Nada heredado de un properties olvidado.

## 2. Backend

| Dato | Tipo Java | Columna | JSON |
|---|---|---|---|
| Fecha + hora | `LocalDateTime` | `TIMESTAMP`, `created_at` | `"17-09-2026 21:30:00"` |
| Solo fecha | `LocalDate` | `DATE`, `due_date` | `"17-09-2026"` |

PROHIBIDO `java.util.Date`, `Calendar`, `java.sql.Timestamp`, `String` en la entidad.

```java
// Entidad — hora de la JVM al persistir (ver Game.onCreate)
@Column(name = "created_at", nullable = false, updatable = false)
private LocalDateTime createdAt;

@PrePersist
void onCreate() { this.createdAt = LocalDateTime.now(); }

// DTO — el formato es parte del contrato del campo (ver GameDTO, MoveDTO)
@JsonProperty("created_at")
@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
private LocalDateTime createdAt;

@JsonProperty("due_date")
@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
private LocalDate dueDate;
```

- **Bug silencioso #1**: `pattern = "dd-MM-yyyy"` sobre un `LocalDateTime` descarta la hora
  sin error. `LocalDateTime` ⇒ el pattern SIEMPRE incluye `HH:mm:ss`.
- Hora argentina: la JVM del contenedor usa UTC por defecto. Si el enunciado exige hora local,
  setear `TZ=America/Argentina/Buenos_Aires` en el `environment` del servicio `backend` en
  compose (config de despliegue, no de Jackson) o usar `LocalDateTime.now(ZoneId.of(...))`
  en un único `Clock` inyectado.
- Ordenar por fecha **en la query** (`order by g.createdAt desc`), no en memoria.
- Comparar con `isBefore` / `isAfter`, nunca strings.

## 3. Frontend

`"dd-MM-yyyy HH:mm:ss"` **no es ISO**: `new Date("17-09-2026")` da `Invalid Date` (o una
fecha equivocada según el navegador). Por eso:

- **Mostrar**: el string ya viene formateado → `{{ move.created_at }}` sin `DatePipe`.
- **Modelo**: `created_at: string` (no `Date`).
- **Operar** (ordenar, comparar, calcular): helper explícito, nunca el string crudo a `new Date`.

```ts
/** Parses the backend contract "dd-MM-yyyy[ HH:mm:ss]" into a local Date. */
export function parseArgentineDate(value: string): Date {
  const [datePart, timePart] = value.split(' ');
  const [day, month, year] = datePart.split('-').map(Number);
  const [hours, minutes, seconds] = (timePart ?? '0:0:0').split(':').map(Number);
  return new Date(year, month - 1, day, hours, minutes, seconds);
}
```

- **Inputs** `type="date"` trabajan con `yyyy-MM-dd`: convertir al cargar (API → input) y al
  enviar (input → API). No bindear el string de la API directo al control.

## 4. Diagnóstico rápido

| Síntoma | Causa | Arreglo |
|---|---|---|
| Se perdió la hora | pattern sin `HH:mm:ss` en un `LocalDateTime` | corregir el `@JsonFormat` |
| `Invalid Date` en el front | `new Date()` sobre `dd-MM-yyyy` | `parseArgentineDate` |
| Corrida 3 horas | JVM/contenedor en UTC | `TZ` del contenedor o `Clock` inyectado |
| Orden raro | ordenando strings `dd-MM-yyyy` | ordenar en la query o con el helper |
| Un día menos en el form | `new Date('yyyy-MM-dd')` = UTC medianoche | trabajar con el string |

## Checklist

- [ ] `LocalDate`/`LocalDateTime` en entidades; cero `Date`/`String`
- [ ] `@JsonFormat` en CADA campo temporal del DTO; `LocalDateTime` con hora
- [ ] Sin config global de fechas en properties ni `LOCALE_ID` global
- [ ] FE: modelo `string`, se muestra tal cual, se parsea con helper para operar
- [ ] Orden por fecha en la query
