---
name: springboot-layered-api
description: >
  API REST en capas con Spring Boot 4 + Java 21 para el parcial: controllers delgados, services
  por interfaz (DIP), mappers manuales, ErrorApi + GlobalExceptionHandler con status semánticos,
  @Transactional en el service, ownership desde el token, auth con cookie HttpOnly + Bearer,
  perfiles dev/test/docker. PROHIBIDO @Autowired, var, record, ModelMapper.
  Trigger: al crear o editar controllers, services, repositories, DTOs, mappers, exceptions,
  configs o clientes de API externa — incluye TODO endpoint de juego (crear partida, tirar
  dado, mover, pasar turno) aunque el usuario no nombre la capa.
license: Apache-2.0
metadata:
  author: PazLuciano
  version: "2.0"
---

# springboot-layered-api

El ejemplo vivo es el juego base: `GameController` → `GameService` (interfaz) →
`GameServiceImpl` → repos + `services/game/*`. **Copiá el patrón al lado, no lo reinventes.**

## 1. Capas y responsabilidad (SRP)

| Paquete | Hace | NUNCA |
|---|---|---|
| `controllers` | recibe, `@Valid`, delega, devuelve `ResponseEntity<DTO>` | lógica, try/catch, repos |
| `services` + `impl` | reglas de negocio, `@Transactional`, ownership | exponer entidades |
| `services/game` | motor del juego (State/Strategy/Chain) — ver skill `game-engine` | tener estado propio |
| `repositories` | queries (`@Query` + `JOIN FETCH`) | lógica |
| `mappers` | entidad ↔ DTO, `@Component` manual | `new` en el service |
| `dtos` | contrato JSON (`@JsonProperty`), validación | entidades JPA adentro |
| `exceptions` | excepciones de dominio + `GlobalExceptionHandler` | `Map<String,Object>` |
| `configs` | beans, interceptor JWT, cookie | reglas de negocio |
| `external` | cliente de API externa aislado | que otra capa use `RestClient` |

- Todo service es **interfaz + impl**; controllers y tests dependen de la interfaz.
- Inyección: `private final` + `@RequiredArgsConstructor`. `@Value` va en el constructor
  (ver `JwtTokenService`, `AuthCookieFactory`).

## 2. Controller delgado (copiar de `GameController`)

```java
@PostMapping("/{id}/roll")
public ResponseEntity<RollResultDTO> roll(
        @RequestAttribute(JwtAuthInterceptor.USER_ID_ATTRIBUTE) Long userId,   // del TOKEN
        @PathVariable Long id,
        @Valid @RequestBody RollRequest request) {
    return ResponseEntity.ok(gameService.roll(id, userId, request));
}
```

| Operación | Respuesta |
|---|---|
| GET | `200` + DTO |
| POST crear | `201` + `Location` (`ResponseEntity.created(URI)`) |
| POST acción (start, roll) / PUT | `200` + DTO actualizado |
| DELETE | `204` |

## 3. Errores — UN contrato: `ErrorApi`

El controller **nunca** atrapa excepciones: el service lanza una excepción de dominio y
`GlobalExceptionHandler` la traduce.

| Status | Cuándo | Excepción del scaffold |
|---|---|---|
| 400 | Bean Validation, body malformado, regla de input | `MethodArgumentNotValidException`, `HttpMessageNotReadableException` |
| 401 | sin token / token inválido / login fallido | `UnauthorizedException`, `InvalidCredentialsException` |
| 404 | no existe **o no es tuyo** (no revelar ids ajenos) | `GameNotFoundException`, `PlayerNotFoundException` |
| 409 | conflicto de estado del juego, concurrencia | `InvalidGameStateException`, `InvalidMoveException`, `ObjectOptimisticLockingFailureException` |
| 500 | no previsto — handler genérico AL FINAL, mensaje genérico | `Exception` |

Nueva excepción = clase en `exceptions/` + agregarla al `@ExceptionHandler` del status que
corresponde. Detalle y casos reales: [references/errores-y-status.md](references/errores-y-status.md).

## 4. Service: transacción, ownership, orquestación

```java
@Transactional                         // escritura → todo o nada
public GameDTO start(Long gameId, Long userId) {
    Game game = loadOwnedGame(gameId, userId);          // JOIN FETCH + ownership
    gameStateRegistry.get(game.getStatus()).start(game); // la regla vive en el State
    return gameMapper.toDTO(game);                       // dirty checking persiste
}
```

- `@Transactional(readOnly = true)` en lecturas; `@Transactional` en escrituras o multi-repo.
  Nunca en controller ni repository.
- **Ownership**: el `userId` sale del token (`@RequestAttribute`), NUNCA del body. Recurso
  ajeno → `GameNotFoundException` (404).
- `spring.jpa.open-in-view=false`: todo lo lazy se carga DENTRO del service.

## 5. DTOs y mappers

- DTO = clase Lombok `@Data @NoArgsConstructor @AllArgsConstructor @Builder` (no `record`).
- Request ≠ Response (`GameCreateRequest` vs `GameDTO`). Validación Jakarta en el request;
  regex **idénticos** a los del FE (`Validators.pattern`).
- JSON en snake_case forzado con `@JsonProperty("board_size")`. Fechas con `@JsonFormat`
  por campo (skill `fechas-argentina-fullstack`).
- Mapper manual `@Component` (ver `GameMapper`); el service lo recibe por constructor.
- Detalle + API externa: [references/dtos-mappers-api-externa.md](references/dtos-mappers-api-externa.md).

## 6. Auth del scaffold (no reescribir, adaptar)

- `POST /api/auth/register|login` → token en el body **y** cookie `AUTH_TOKEN`
  (`HttpOnly; SameSite=Strict; Path=/api`). `POST /api/auth/logout` borra la cookie.
  `GET /api/auth/me` devuelve el usuario de la sesión.
- `JwtAuthInterceptor` acepta `Authorization: Bearer` (Postman/corrector) o la cookie (SPA).
  Rutas protegidas: `WebConfig.addPathPatterns(...)` — agregar las del enunciado.
- Seguridad mínima: BCrypt; mismo mensaje para usuario inexistente y password malo; el
  password nunca sale en un DTO; secretos por variable de entorno (`APP_JWT_SECRET`).

## 7. Perfiles y configuración

| Perfil | DB | ddl-auto | Uso |
|---|---|---|---|
| default | H2 memoria | `create-drop` | `mvn spring-boot:run`, desarrollo |
| test (`src/test/resources`) | H2 memoria | `create-drop` | `mvn verify` |
| `docker` | PostgreSQL (env) | `update` | `docker compose up` — las partidas sobreviven |

Nada de `data.sql`: el tablero lo arma la `BoardFactory` y los usuarios se registran.
Si el enunciado exige datos iniciales, usar un `CommandLineRunner` idempotente
(`if (repo.count() == 0)`), que funciona igual en H2 y en PostgreSQL.

## Checklist

- [ ] Controller sin lógica ni try/catch; status correctos (201+Location, 204, 409…)
- [ ] Service interfaz + impl; `@Transactional` en el impl; ownership con el id del token
- [ ] Entidades nunca salen del service; mapper manual inyectado
- [ ] `@JsonProperty` en cada campo cuyo JSON difiere del nombre Java
- [ ] Excepción de dominio nueva registrada en el handler con su status
- [ ] Rutas nuevas protegidas en `WebConfig`
- [ ] Sin `@Autowired`, `var`, `record`, `ModelMapper`, `System.out`
