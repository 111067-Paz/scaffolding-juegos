# Mockito: @Mock, @Spy, @InjectMocks, when/verify/assertThrows

> Referencia de la skill java-testing (versión más nueva de java-testing-PazLuciano).

## 2. Anotaciones Mockito — cuándo usar cada una

### `@Mock` — La regla general

Objeto 100% falso. Todos sus métodos devuelven null/0/false salvo que se configuren con `when()`.

**Usar para:** repositorios, servicios externos, APIs, cualquier dependencia que no queremos ejecutar realmente.

```java
@Mock
private UserRepository userRepository;
```

### `@Spy` — La excepción

Objeto **real** que Mockito espía. Ejecuta la lógica verdadera pero permite `verify()` y sobrescribir métodos puntuales con `doReturn()`.

**Usar cuando:**
- La dependencia tiene lógica real que queremos mantener (ej: un Mapper, un validador).
- Necesitamos verificar cuántas veces se llamó a un método real.

**No usar** `@Spy` sobre la misma clase que tiene `@InjectMocks`.

```java
@Spy
private UserMapper userMapper = new UserMapperImpl();
```

### `@InjectMocks` — El sujeto de prueba

La clase real que estamos probando. Mockito inyecta automáticamente todos los `@Mock` y `@Spy` definidos en la clase de test.

**Regla:** solo una por clase de test. No funciona con objetos creados con `new`.

```java
@InjectMocks
private UserService userService;
```

---

## 3. Métodos de configuración de comportamiento

### `when().thenReturn()`

Configura qué devuelve un método del mock cuando es llamado.

```java
when(userRepository.findById(1L)).thenReturn(Optional.of(fakeUser));
```

### `when().thenThrow()`

Configura el mock para que lance una excepción. **Siempre** combinar con `assertThrows()` en el THEN.

```java
when(userRepository.findById(99L))
    .thenThrow(new RuntimeException("Error de conexión a BD"));
```

### `doNothing().when(mock).metodo()`

Para métodos `void` que no queremos que hagan nada (ej: enviar un email real).

```java
doNothing().when(emailService).sendEmail(anyString());
```

---

## 4. Verificación con `verify()`

Comprueba que un método del mock/spy fue (o no fue) llamado, con qué argumentos y cuántas veces.

```java
verify(userRepository, times(1)).findById(1L);      // llamado exactamente 1 vez
verify(emailService, never()).sendEmail(anyString()); // nunca llamado
verify(userMapper, atLeastOnce()).toDto(any());       // al menos 1 vez
verifyNoInteractions(miMapper);                       // ningún método fue llamado
```

---

## 5. Manejo de excepciones — `assertThrows`

**Regla:** cuando un mock lanza excepción, SIEMPRE capturar con `assertThrows` en el WHEN.
Nunca dejar que el test falle abruptamente.

```java
@Test
void processUser_whenDatabaseFails_shouldThrowRuntimeException() {
    // GIVEN
    when(userRepository.findById(1L))
        .thenThrow(new RuntimeException("Error de conexión a BD"));

    // WHEN & THEN
    RuntimeException ex = assertThrows(RuntimeException.class, () -> {
        userService.processUser(1L);
    });

    assertEquals("Error de conexión a BD", ex.getMessage());
    verify(userRepository, times(1)).findById(1L);
}
```

---

## 6. Pruebas en métodos privados — Reflection

Los métodos privados no son accesibles directamente desde tests. La cátedra enseña dos enfoques:

### Alternativa preferida: testear indirectamente

Llamar al método público que internamente invoca el privado. Si el privado falla, el test del público falla. Es la opción limpia.

### Cuando no queda otra: `ReflectionSupport` (JUnit 5)

Usar `org.junit.platform.commons.support.ReflectionSupport`. Es parte de JUnit 5 y más limpio que la API nativa de `java.lang.reflect`.

**Invocar un método privado:**

```java
import org.junit.platform.commons.support.ReflectionSupport;

@Test
void testMetodoPrivado() {
    // GIVEN
    MiClase instancia = new MiClase();

    // WHEN
    Object resultado = ReflectionSupport.invokeMethod(
        ReflectionSupport.findMethod(MiClase.class, "metodoPrivado").get(),
        instancia
        // argumentos adicionales si los tiene
    );

    // THEN
    assertEquals(valorEsperado, resultado);
}
```

**Métodos clave de `ReflectionSupport`:**

| Método | Uso |
|---|---|
| `findMethod(Class, String)` | Busca un método por nombre. Devuelve `Optional<Method>`. |
| `invokeMethod(Method, Object, Object...)` | Invoca el método en una instancia. |
| `findField(Class, String)` | Busca un campo privado. Devuelve `Optional<Field>`. |
| `getFieldValue(Field, Object)` | Obtiene el valor de un campo privado. |
| `setFieldValue(Field, Object, Object)` | Establece el valor de un campo privado. |

> **Aclaración al usuario (modo mixto):** cuando se use Reflection, mencionar que es una herramienta de la cátedra para casos donde no se puede testear indirectamente, y que en proyectos reales se prefiere refactorizar el código.

---
