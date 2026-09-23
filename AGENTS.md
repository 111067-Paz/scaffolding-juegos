# AGENTS.md — Reglas del proyecto (portable: Claude Code, Antigravity, Cursor, OpenCode…)

> Fuente de verdad de reglas. Las skills (`.claude/skills/`, copia en `.agents/skills/`) tienen
> el detalle y ejemplos; los comandos (`.claude/commands/`, copia en `.agents/workflows/`) el
> flujo de trabajo. Todos los links de este repo son relativos: sin rutas de una PC.

## 0. Principios

1. **El humano dirige la arquitectura; el agente ejecuta y explica el PORQUÉ.** Ante algo mal
   pedido: frenar, explicar con evidencia y proponer la alternativa.
2. **Modelo antes que código**: entidades, relaciones y reglas validadas antes de escribir.
3. **Rebanadas verticales**: una feature de punta a punta (BE → tests → FE) por fase.
4. **La continuidad vive en el repo** (`PLAN.md` + `PROGRESS.md` + git), no en la memoria de la IA.
5. **El juego base es la referencia viva**: copiar el patrón hermano, no inventar otro.

## 1. Estructura

- `BE/` Spring Boot 4 · Java 21 · Maven · H2 (dev/test) · PostgreSQL (docker). Paquete base
  `ar.edu.utn.frc.tup.p4`. Capas: `controllers` · `services` (+`impl`) · `services/game`
  (`state`, `effects`, `validation`) · `repositories` · `entities` · `dtos` · `mappers` ·
  `exceptions` · `external` · `configs`.
- `FE/` Angular 21 standalone zoneless + Tailwind 4: `core/` + `features/{x}/pages·ui·data-access`.
- `docker-compose.yml`: `db` (PostgreSQL) → `backend` (perfil `docker`) → `frontend` (Nginx, `:4200`).
- **Idioma**: código, identificadores, comentarios y Javadoc/JSDoc en **inglés**. Textos de UI
  en español. Docs del repo en español.

## 2. Backend — reglas absolutas

- **PROHIBIDO** `@Autowired` (en cualquier forma, también en tests), `var`, `record`,
  `ModelMapper`, `@Data` en entidades, `double` para dinero, `System.out`, `@EntityGraph`, EAGER.
- Inyección: `private final` + `@RequiredArgsConstructor`. Tests Spring: constructor +
  `@TestConstructor(autowireMode = ALL)`.
- **Todo service = interfaz + `impl/`**; controllers y tests dependen de la interfaz.
- DTOs: clases Lombok `@Data @NoArgsConstructor @AllArgsConstructor @Builder`; JSON exacto con
  `@JsonProperty`; fechas con `@JsonFormat` por campo (sin config global).
- Entidades: `@Getter @Setter @NoArgsConstructor @AllArgsConstructor`; relaciones LAZY,
  `@ForeignKey` nombrado, `mappedBy` en el inverso; N+1 con `@Query` + `JOIN FETCH`.
- Mapper manual `@Component` inyectado; nunca mapeo inline ni `new` en el service.
- `@Transactional` en el service (escrituras / multi-repo; `readOnly` en lecturas).
- Errores: excepciones de dominio → `GlobalExceptionHandler` → `ErrorApi`. El controller NUNCA
  hace try/catch. Status: 200 · 201+Location · 204 · 400 · 401 · 404 · 409 · 500 (genérico, último).
- **Ownership**: el id de usuario sale del token (`@RequestAttribute(USER_ID_ATTRIBUTE)`),
  NUNCA del body. Recurso ajeno → 404.
- **Estado del juego SIEMPRE en entidades JPA.** PROHIBIDO `Map` estático / singleton con
  estado. `@Version` en la raíz del agregado (concurrencia → 409).
- Secretos por variables de entorno; `.env` nunca se commitea.

## 3. Patrones (la cátedra los valora cuando APORTAN)

| Problema del enunciado | Patrón | En el scaffold |
|---|---|---|
| Fases con comportamiento distinto | **State** | `services/game/state` |
| Tipos (casillas, cartas…) con efecto distinto | **Strategy + Registry** | `services/game/effects` |
| Armado según reglas (tablero, mazo) | **Factory** | `BoardFactory` |
| Validaciones encadenadas | **Chain of Responsibility** (`@Order`) | `services/game/validation` |
| Azar | interfaz inyectable | `DiceRoller` |

Regla de oro: el patrón se justifica por una frase del enunciado. 2 casos estables = `if`.
Streams para transformaciones puras; `for`/`if` para algoritmos con orden y efectos.

## 4. Frontend — reglas absolutas

- **PROHIBIDO**: `NgModule`, `*ngIf/*ngFor/*ngSwitch`, `@Input/@Output/@ViewChild`, inyección
  por constructor, signals no `readonly`, `BehaviorSubject` para estado de UI, métodos llamados
  desde el template, `any`, URLs absolutas (`http://localhost…`), `console.log` (usar
  `LoggerService`), `localStorage`/`sessionStorage` para tokens, `[(ngModel)]`, `zone.js`, Karma.
- Usar: `input()`/`output()`/`model()`, `inject()`, `signal`/`computed`/`linkedSignal`,
  `@if/@for(track)+@empty/@switch/@defer`, `@let`, `[class.x]`.
- HTTP solo en `data-access/*.service.ts`: **lecturas con `httpResource`**, comandos con
  Observables + `catchError`. Base URL = `environment.apiUrl` (`/api`).
- Pages = smart (resources, comandos, `computed`); `ui/` = dumb (solo inputs/outputs).
- Formularios: Reactive Forms tipados `nonNullable`; errores tras `touched`; **regex idénticos
  al backend**; cross-field a nivel `FormGroup`.
- Rutas lazy (`loadComponent`/`loadChildren`) con guards funcionales.
- Auth: cookie HttpOnly (el FE nunca ve ni guarda el token); sesión restaurada con `/api/auth/me`.

## 5. Testing y Definition of Done

- JUnit 5 + Mockito: naming `method_scenario_expected`, `@DisplayName`, `@Tag("unit"|"integration")`,
  GIVEN/WHEN/THEN, mockear TODAS las dependencias (mappers incluidos), camino de error siempre.
- Slice web: `standaloneSetup` + `GlobalExceptionHandler`. Boot 4: `@MockitoBean`.
- **JaCoCo ≥ 95% de líneas**: `./mvnw verify` falla por debajo. FE: Vitest (`npm run test:ci`).

Una fase está TERMINADA cuando:
- [ ] `./mvnw -f BE/pom.xml verify` verde (tests + coverage check)
- [ ] `npm --prefix FE run test:ci` y `npm --prefix FE run build` verdes
- [ ] `/auditar` sin prohibidos
- [ ] El flujo se probó de punta a punta (dev o `docker compose up --build`)
- [ ] `PLAN.md` actualizado y mensaje de commit sugerido

## 6. Protocolo del agente

- Al iniciar: leer `PROGRESS.md`, `PLAN.md` y `ENUNCIADO.md`.
- Ambigüedad en el enunciado → **PARAR y preguntar** señalando qué es ambiguo. No inventar requisitos.
- Al terminar cada fase: **detenerse**, resumir, sugerir commit y esperar OK. No encadenar fases.
- El humano maneja git (commits/push) salvo que pida lo contrario. Conventional Commits en
  inglés (`feat(game): ...`); un commit = una unidad entregable con sus tests; **sin
  atribución de IA** en los mensajes.
- No agregar dependencias sin avisar. No borrar código que funciona sin avisar.
- Nunca comandos destructivos (`rm -rf` amplio, `git push --force`, `git reset --hard`).
- Antes de cerrar la sesión: actualizar `PROGRESS.md` (hecho, decisiones, próximos pasos).

## 7. Mapa contexto → skill (leer ANTES de escribir código)

| Contexto | Skill |
|---|---|
| Controllers, services, DTOs, mappers, errores, auth, perfiles | `springboot-layered-api` |
| Entidades, relaciones, queries, N+1, `@Version` | `jpa-modelado` |
| Cualquier método Java (reglas, colecciones, streams vs for) | `java21-clean-code` |
| Lógica de juego, patrones, adaptar el juego base | `game-engine` |
| Tests Java, Mockito, JaCoCo | `java-testing` |
| Componentes, services, rutas, forms, tests Angular | `angular-spa` |
| Templates, Tailwind, ARIA, teclado, responsive | `html-accesible-responsive` |
| Dockerfile, compose, Nginx, `.env`, PostgreSQL | `docker-stack` |
| Campos de fecha/hora en cualquier capa | `fechas-argentina-fullstack` |
| Crear proyecto o artefactos nuevos | `cli-first-scaffolding` |

## 8. Comandos / workflows

`/arranque` (enunciado → modelo → PLAN.md) · `/fase N` (una rebanada vertical) ·
`/verificar` (build + tests + coverage) · `/auditar` (grep de prohibidos) ·
`/entrega` (clone limpio + docker + smoke). En herramientas sin comandos: leer el archivo
equivalente en `.agents/workflows/` y seguirlo.
