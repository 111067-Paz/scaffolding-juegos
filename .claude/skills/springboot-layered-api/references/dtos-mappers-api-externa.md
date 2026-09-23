# DTOs, mappers y API externa

> Referencia de la skill springboot-layered-api. El ejemplo vivo del scaffold es GameMapper y external/ExternalApiClient (+ su test con MockRestServiceServer).

## 5. DTOs y mappers

**DTOs como clases con Lombok** (regla del parcial: NO usar `record`):

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardSearchRequest {
    @Size(max = 10, message = "Set code must not exceed 10 characters")
    private String setCode;

    @Size(max = 50, message = "Search name must not exceed 50 characters")
    private String name;
}
```

- Bean Validation (`@NotNull`, `@NotBlank`, `@Size`, `@Positive`) va en los DTOs de request,
  con `message` claro. Se activa con `@Valid` en el controller.
- En entidades JPA usar `@Getter @Setter @NoArgsConstructor` — **NO `@Data`** (equals/hashCode
  con relaciones lazy provoca errores y queries accidentales).

**Validación robusta: `@Pattern` y `@Email(regexp=...)`**

> ⚠️ `@Email` por defecto es LAXO: acepta `asad@sa` (sin TLD). Para exigir `dominio.tld`
> pasale un `regexp`. Y para formatos (username, password) usá `@Pattern`. **Los regex deben
> ser IDÉNTICOS a los del frontend** (`Validators.pattern`): el contrato de validación coincide
> en ambas capas — el backend NUNCA confía solo en el cliente.

```java
@NotBlank(message = "Username is required")
@Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
@Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]*$",
        message = "Username must start with a letter and use only letters, numbers or underscore")
private String username;

@NotBlank(message = "Email is required")
@Email(regexp = "^[\\w.%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$",   // exige dominio.tld, rechaza "asad@sa"
        message = "Email must be valid (e.g. name@domain.com)")
private String email;

@NotBlank(message = "Password is required")
@Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",   // mayúscula + minúscula + número
        message = "Password must include an uppercase letter, a lowercase letter and a number")
private String password;
```

- En el regex de Java escapá la barra: `\\d`, `\\.`, `\\w` (en el regex de TS van con una sola: `\d`).
- Login NO re-valida complejidad (un usuario viejo puede tener otra política): su request solo
  lleva `@NotBlank`. La política fuerte va en el alta (`RegisterRequest`).

**Mapper = clase propia en `mappers/`**, no lógica inline en el service:

```java
@Component
public class CardDtoMapper {
    public CardDTO toDTO(Card entity) { ... }
    public List<CardDTO> toDTOList(List<Card> entities) {
        return entities.stream().map(this::toDTO).toList();
    }
}
```

El service inyecta el mapper. Si el mapeo cambia, se toca UNA clase y se testea UNA clase.

**ModelMapper está PROHIBIDO en este scaffold** (reflexión: falla en runtime, no en compilación). Mapper manual `@Component` inyectado por constructor, siempre.

---


## 6. API externa — aislada en `external/`

- Solo `external/` llama a la API externa. El resto del sistema consume el cache local.
- El cliente devuelve DTOs de respuesta propios (`PokemonTcgCardResponse`), que un mapper
  convierte a entidades. La API externa NUNCA dicta tu modelo de dominio.
- En tests, **mockear el cliente** — jamás llamar a la API real.

### ✅ BIEN — caso real: import batch sin N+1

```java
@Transactional
@CacheEvict(value = {"cards", "cardsList"}, allEntries = true)
public void loadCardsFromApi(String setCode) {
    List<PokemonTcgCardResponse> responses = pokemonTcgApiClient.getCardsBySet(setCode);

    // UNA query trae los existentes; Set da lookup O(1)
    Set<String> existingApiIds = cardRepository.findBySetCode(setCode)
        .stream().map(Card::getApiId).collect(Collectors.toSet());

    List<Card> newCards = responses.stream()
        .filter(response -> !existingApiIds.contains(response.getId()))
        .map(cardMapper::toEntity)
        .toList();

    if (!newCards.isEmpty()) { cardRepository.saveAll(newCards); }
}
```

### ❌ MAL — N+1: una query por elemento

```java
for (PokemonTcgCardResponse response : responses) {
    if (!cardRepository.existsByApiId(response.getId())) {  // ❌ N queries
        cardRepository.save(cardMapper.toEntity(response)); // ❌ N inserts sueltos
    }
}
```

---
