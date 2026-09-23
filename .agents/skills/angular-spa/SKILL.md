---
name: angular-spa
description: >
  SPA Angular 21 del scaffold: standalone, zoneless, Signals (signal/computed/linkedSignal,
  readonly), input()/output()/model() (sin decoradores), inject() (sin constructor DI),
  control flow @if/@for(track+@empty)/@switch/@defer, estructura core/ + features/{x}/
  pages·ui·data-access (smart/dumb), httpResource para lecturas y Observables para comandos,
  environment.apiUrl, auth con cookie HttpOnly, Reactive Forms tipados, rutas lazy con guards
  funcionales, LoggerService (sin console.log) y tests con Vitest.
  Trigger: al crear o editar componentes, servicios, rutas, guards, pipes, interceptors,
  formularios o modelos en Angular/TypeScript — incluye TODA pantalla de un juego (lobby,
  tablero, login, listado de partidas) aunque el usuario no nombre Angular.
license: Apache-2.0
metadata:
  author: PazLuciano
  version: "2.0"
---

# angular-spa

Ejemplo vivo: `FE/src/app/features/game` (smart pages + dumb ui + data-access).

## 1. Estructura

```
src/app/
├── core/          singletons, una vez: auth/ (service, guard, models) · http/ (interceptor,
│                  api-error) · logger/ · models/ (ErrorApi) · layout/ (not-found)
├── features/{x}/  una carpeta por dominio
│   ├── pages/        SMART: inyectan services, tienen resources/comandos, van en rutas
│   ├── ui/           DUMB: solo input()/output(), sin services ni HTTP
│   ├── data-access/  {x}.service.ts (único que usa HttpClient) + {x}.models.ts
│   └── {x}.routes.ts rutas hijas lazy
└── app.config.ts · app.routes.ts · app.ts
```

Generar con el CLI (skill `cli-first-scaffolding`): `ng g c features/game/ui/dice-panel`.

## 2. PROHIBIDO → usar

| PROHIBIDO | Usar |
|---|---|
| `NgModule`, `standalone: true` explícito | standalone por defecto |
| `*ngIf` / `*ngFor` / `*ngSwitch` | `@if` / `@for (x of xs(); track x.id) {} @empty {}` / `@switch` / `@defer` |
| `@Input()` / `@Output()` / `@ViewChild()` | `input()`, `input.required()`, `output()`, `model()`, `viewChild()` |
| `constructor(private s: S)` | `private readonly s = inject(S)` |
| signals/inputs mutables | `readonly isLoading = signal(false)` |
| `BehaviorSubject` para estado de UI | `signal` / `computed` / `linkedSignal` |
| método llamado desde el template | `computed()` o `@let` en el template |
| `any` | interfaces que espejan los DTOs; `unknown` + type guard |
| `http://localhost:8080` / IPs | `environment.apiUrl` (`/api`) |
| `console.log` | `LoggerService` (sanitiza password/token/secret) |
| `localStorage` para token | cookie HttpOnly (el FE nunca ve el token) |
| `[(ngModel)]` | Reactive Forms tipados `nonNullable` |
| `ngClass` / `ngStyle` | `[class.x]` / `[style.x]` |
| `zone.js`, Karma | zoneless (`provideZonelessChangeDetection()`), Vitest |

## 3. Datos: httpResource (lecturas) vs Observables (comandos)

```ts
// data-access/game.service.ts — el ÚNICO lugar con HTTP
gameResource(gameId: () => number | undefined): HttpResourceRef<Game | undefined> {
  return httpResource<Game>(() => {
    const id = gameId();
    return id === undefined ? undefined : `${this.gamesUrl}/${id}`;  // undefined = no pedir
  });
}
roll(gameId: number, playerId: number): Observable<RollResult> {
  return this.http.post<RollResult>(`${this.gamesUrl}/${gameId}/roll`, { player_id: playerId })
    .pipe(catchError(toUserError));
}

// pages/game-board-page.ts — smart
readonly id = input.required<string>();                     // param de ruta (withComponentInputBinding)
readonly gameId = computed(() => Number(this.id()));
readonly game = this.gameService.gameResource(() => this.gameId());   // re-pide si cambia el id
readonly currentPlayer = computed(() => /* derivado de this.game.value() */);
```

- Template: `game.isLoading()`, `game.error()`, `game.value()`, `game.hasValue()`.
- Después de un comando: `this.game.set(result.game)` (respuesta del server) o `reload()`.
- Suscripciones manuales solo para comandos, con `takeUntilDestroyed(this.destroyRef)`.
- Referencias: [references/resource.md](references/resource.md), [references/linked-signal.md](references/linked-signal.md).

## 4. Smart / dumb

```ts
@Component({ selector: 'app-dice-panel', template: `...` })
export class DicePanel {                                  // dumb: sin inject() de services
  readonly playerName = input.required<string>();
  readonly lastValue = input<number | null>(null);
  readonly disabled = input(false);
  readonly roll = output<void>();
}
```

```html
<app-dice-panel [playerName]="currentPlayer()?.name ?? ''" [disabled]="busy()" (roll)="roll()" />
```

Detalle: [references/inputs.md](references/inputs.md), [references/outputs.md](references/outputs.md).
`OnPush` es opcional (zoneless ya actualiza solo por signals); no se exige.

## 5. Templates

- `@for` SIEMPRE con `track` (id estable) y `@empty`.
- `@let control = form.controls.username;` para no repetir expresiones ni llamar métodos.
- Modales/paneles pesados dentro de `@if` (no `[hidden]`): se destruyen al cerrarse.
- `@defer (on idle)` para bloques secundarios (historial).
- Lookup tables para presentación por tipo (`CELL_META[cell.type]`), no `@switch` gigantes.

## 6. Auth, HTTP y errores

- `AuthService`: `currentUser`/`sessionStatus` como signals readonly; `login/register` abren
  sesión con el `user` de la respuesta (se ignora el token); `restoreSession()` → `/auth/me`.
- `authGuard` (funcional): si el estado es `unknown`, pregunta `/me` una vez. Es UX: la
  seguridad real está en el backend.
- `errorInterceptor`: 401 en llamada protegida → `clearSession()` + `/login`; loguea con
  `LoggerService`; re-lanza. Mensajes al usuario con `extractErrorMessage(error)` (ErrorApi).
- `provideHttpClient(withFetch(), withInterceptors([errorInterceptor]))`: same-origin → la
  cookie viaja sola, sin interceptor de token.

## 7. Formularios reactivos

Tipados, `nonNullable`, validación en TS, errores visibles tras `touched`, regex **idénticos**
al backend (`PLAYER_NAME_PATTERN` ↔ `@Pattern`), cross-field a nivel `FormGroup`,
`FormArray` para listas (jugadores). Referencia: [references/formularios-reactivos.md](references/formularios-reactivos.md).

## 8. Rutas

Todas lazy (`loadComponent` / `loadChildren` → `GAME_ROUTES`), `canActivate: [authGuard]` en el
padre, wildcard al final, `withComponentInputBinding()` para params como `input()`.

## 9. Tests (Vitest, `npm run test:ci`)

| Qué | Cómo |
|---|---|
| Service HTTP | `provideHttpClient()` + `provideHttpClientTesting()` + `HttpTestingController.expectOne` |
| httpResource | `TestBed.runInInjectionContext(() => service.gameResource(() => id()))` + `ApplicationRef.tick()` |
| Guard / interceptor | `TestBed.runInInjectionContext(() => authGuard(...))`; `withInterceptors([...])` |
| Dumb component | `fixture.componentRef.setInput('cells', [...])` + `await fixture.whenStable()` |
| Form | instanciar la page y probar reglas/regex/límites sin DOM |

## Checklist

- [ ] Nada de la tabla PROHIBIDO; `npm run build` sin warnings
- [ ] HTTP solo en `data-access/`; GET con `httpResource`; URLs desde `environment.apiUrl`
- [ ] Modelos con las keys EXACTAS del JSON (snake_case)
- [ ] Pages smart / ui dumb; todo lo derivado en `computed()`
- [ ] `@for` con `track` + `@empty`; sin métodos en templates
- [ ] Specs del service, guard y componentes nuevos en verde
