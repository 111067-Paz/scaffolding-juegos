# Errores, status HTTP y excepciones de dominio

> Referencia de la skill springboot-layered-api. Casos reales de proyectos anteriores.

## 4. Manejo de errores — UN solo contrato tipado

UNA clase `ErrorApi` (DTO con Lombok) para TODAS las respuestas de error. El frontend debe
recibir SIEMPRE el mismo formato.

### ✅ BIEN

```java
@ExceptionHandler(CardNotFoundException.class)
public ResponseEntity<ErrorApi> handleCardNotFound(CardNotFoundException exception) {
    log.warn("Card not found: {}", exception.getMessage());
    ErrorApi errorResponse = ErrorApi.builder()
            .timestamp(LocalDateTime.now().format(FORMATTER))
            .status(HttpStatus.NOT_FOUND.value())
            .error(HttpStatus.NOT_FOUND.getReasonPhrase())
            .message(exception.getMessage())
            .build();
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
}
```

### ❌ MAL — caso real encontrado en el proyecto tpi-pokemon

```java
// ❌ Mitad de los handlers devolvían ErrorApi y la otra mitad Map<String,Object>:
@ExceptionHandler(UserNotFoundException.class)
public ResponseEntity<Map<String, Object>> handleUserNotFound(UserNotFoundException ex) {
    return error(HttpStatus.NOT_FOUND, ex.getMessage());   // ❌ payload sin tipo
}
```

**Por qué está mal**: `Map<String, Object>` no tiene contrato — Swagger no lo documenta, el
compilador no detecta un typo en una key, y la API termina con dos formatos de error distintos
según qué excepción salte. Mismo anti-patrón que usar `Map` como request body.

### Mapeo correcto excepción → HTTP status

| Situación | Excepción | Status |
|-----------|-----------|--------|
| Recurso no existe | `XxxNotFoundException` | 404 |
| Request inválido / validación de negocio | `IllegalArgumentException`, validación | **400** (o 422) |
| Bean Validation falla | `MethodArgumentNotValidException` | 400 |
| Conflicto de estado / concurrencia | `OptimisticLockException` | 409 |
| Sin permisos | `AccessDeniedException` | 403 |
| No autenticado | credenciales inválidas | 401 |
| Error no previsto | `Exception` (handler genérico, SIEMPRE al final) | 500 |

### ❌ MAL — caso real: validación mapeada a 404

```java
@ExceptionHandler(DeckValidationException.class)   // un deck INVÁLIDO...
public ResponseEntity<ErrorApi> handleDeckValidation(DeckValidationException exception) {
    // ❌ ...respondía 404 NOT_FOUND. Una validación fallida NO es "recurso no encontrado".
    .status(HttpStatus.NOT_FOUND.value())          // ✅ debe ser 400 BAD_REQUEST
```

**Regla**: el status code describe QUÉ pasó, no "algo falló". 404 = no existe; 400 = me mandaste
algo inválido. Confundirlos miente al cliente HTTP.

### Excepciones de dominio con nombre correcto

```java
// ❌ MAL — caso real: buscar una CARTA lanzaba GameNotFoundException
.orElseThrow(() -> new GameNotFoundException("Card not found: " + id));

// ✅ BIEN — la excepción nombra el dominio que falla
.orElseThrow(() -> new CardNotFoundException("Card not found: " + id));
```

Cada agregado tiene SU excepción (`CardNotFoundException`, `DeckNotFoundException`). El log y el
handler cuentan la verdad sobre qué faltó.

---
