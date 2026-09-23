# Frontend de juegos con Angular 21 (zoneless + signals)

> Referencia de la skill game-engine (fusiona juegos-angular-springboot con las reglas del
> scaffold). El ejemplo vivo es features/game (DOM, por turnos).

## Superficie de render

| Juego | Render | Por qué |
|---|---|---|
| Por turnos, cartas, tablero (el 95% de los parciales) | **DOM** (`@for` + Tailwind grid) | accesible, testeable, cada pieza es un componente |
| Movimiento continuo (snake, pong, shooter) | `<canvas>` con `viewChild<ElementRef<HTMLCanvasElement>>()` | 60 fps sin tocar el DOM |

## Reglas

1. **El servidor es la autoridad**: el FE muestra el estado que devuelve la API y manda
   intenciones (`roll`, `move`), nunca el resultado. Puntajes/victorias se calculan en el BE.
2. **Smart vs dumb**: la page tiene los `httpResource` y los comandos; los componentes de
   `ui/` reciben `input()` y emiten `output()`. Tablero, dado, panel de jugadores, historial
   son dumb (ver `board-grid`, `dice-panel`).
3. **Estado derivado con `computed()`**: jugador actual, ganador, celdas con fichas. Nada de
   métodos llamados desde el template.
4. **Tras un comando**, actualizar con la respuesta del server
   (`this.game.set(result.game)`) y `reload()` lo secundario (historial).
5. **Lookup tables en vez de switch** para presentación por tipo (`CELL_META`).
6. **Accesibilidad**: cada casilla con `aria-label` que diga qué es y quién está; resultado del
   dado con `aria-live="polite"`; botones reales (`<button>`), foco visible.

## Si hay canvas / game loop (tiempo real)

- Motor en **TypeScript puro** (sin imports de Angular) → testeable con Vitest sin DOM.
- Loop con `requestAnimationFrame` + **delta time** (la velocidad no depende de los FPS);
  nunca `setInterval`.
- Cancelar el loop y los listeners al destruir: `inject(DestroyRef).onDestroy(() => cancelAnimationFrame(id))`.
- Zoneless: la vista solo se entera de cambios por signals. HUD (puntaje, vidas, fase) en
  `signal()`; posiciones por frame **fuera** de signals (se dibujan en el canvas).
- 3D (Three.js): liberar `geometry.dispose()` / `material.dispose()` al descartar mallas.

## Anti-patrones

- Reglas del juego en el `.component.ts` (imposibles de testear).
- El cliente decide quién ganó o manda el puntaje final.
- Un componente dios con tablero + HUD + historial + formulario.
- `setInterval` como game loop; un signal que se escribe 60 veces por segundo.
