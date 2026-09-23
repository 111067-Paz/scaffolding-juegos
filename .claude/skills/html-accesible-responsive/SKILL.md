---
name: html-accesible-responsive
description: >
  HTML semántico, accesibilidad real (teclado, foco, contraste, ARIA solo cuando el HTML nativo
  no alcanza, aria-live para cambios de juego) y responsive mobile-first con Tailwind 4, más
  manejo de DOM/eventos en Angular sin querySelector. Incluye tableros de juego accesibles.
  Trigger: al escribir o editar templates HTML, estilos Tailwind/CSS, atributos ARIA,
  navegación por teclado, layouts responsive o la vista de un tablero/dado/cartas.
license: Apache-2.0
metadata:
  author: PazLuciano
  version: "2.0"
---

# html-accesible-responsive

## 1. El elemento correcto

| Necesito | Elemento |
|---|---|
| Acción (tirar dado, empezar, borrar) | `<button type="button">` |
| Navegación a otra ruta | `<a routerLink>` |
| Estructura de página | `<header>`, `<nav aria-label>`, `<main id="main">`, `<section aria-labelledby>`, `<aside>`, `<footer>` |
| Listas (partidas, jugadores, historial, casillas) | `<ul>` / `<ol>` + `<li>` |
| Formulario | `<form>` + `<label>` que envuelve o apunta al input; `<fieldset>` + `<legend>` para grupos |

- Un `<main>` y un `<h1>` por página; headings sin saltos.
- Cero `<div (click)>`: no tienen foco ni teclado.
- Link "Saltar al contenido" al inicio (ver `app.html`).

## 2. Juego accesible (ejemplo vivo: `board-grid`, `dice-panel`, `player-panel`)

```html
<ol class="grid grid-cols-5 gap-2 sm:grid-cols-8 lg:grid-cols-10" aria-label="Tablero">
  @for (cell of cellViews(); track cell.position) {
    <li [attr.aria-label]="cell.ariaLabel">          <!-- "Casilla 7: Trampa. Jugadores: Ana" -->
      <span aria-hidden="true">{{ cell.icon }}</span> <!-- decorativo → oculto al lector -->
    </li>
  }
</ol>
<p aria-live="polite">{{ lastValue() ?? '–' }}</p>   <!-- anuncia el resultado del dado -->
<li [attr.aria-current]="isCurrent ? 'step' : null"> <!-- de quién es el turno -->
```

- El **estado del juego no puede depender solo del color**: ícono + texto + `aria-label`.
- Cambios que el usuario no provocó con foco (resultado, turno, ganador) → `aria-live="polite"`.
- Errores de formulario con `role="alert"` y `[attr.aria-invalid]`.
- Botones deshabilitados mientras se procesa (`[disabled]="busy()"`) para evitar doble envío.

## 3. Teclado y foco

- Todo operable con Tab / Enter / Espacio; foco visible (`focus-visible:ring-2`).
- Widgets custom (dropdown, grilla navegable por flechas): patrón WAI-ARIA + Angular Aria si
  aplica. Referencia con casos reales: [references/aria-teclado-contraste.md](references/aria-teclado-contraste.md).
- Tras navegar a una pantalla nueva el foco no debe perderse (el router lo maneja; en modales,
  mover el foco al abrir y devolverlo al cerrar).

## 4. Contraste y percepción

- Texto 4.5:1 (AA); grande 3:1. El color nunca es el único indicador.
- `motion-reduce:animate-none` si hay animaciones (dado girando, fichas moviéndose).
- `alt` descriptivo en imágenes informativas; `alt=""` en decorativas.

## 5. Responsive mobile-first (Tailwind 4)

- Sin prefijo = móvil; `sm:` ≥640, `md:` ≥768, `lg:` ≥1024.
- Layout con `grid`/`flex` + `gap`; ancho fluido `mx-auto max-w-6xl px-4`; nunca anchos fijos.
- Tablero: `grid-cols-5 sm:grid-cols-8 lg:grid-cols-10` + `aspect-square` en las casillas.
- Página de juego: `grid lg:grid-cols-[1fr_18rem]` (tablero + panel lateral que baja en móvil).
- Probar en 375 / 768 / 1280 px; touch targets ≥ 44 px.

## 6. DOM y eventos en Angular

- Nunca `document.querySelector` / `getElementById`: bindings, `viewChild()`, template refs.
- Eventos con `(click)`, `(keydown.enter)`; listeners globales en `host: { '(document:keydown)': ... }`
  (Angular los limpia). Nunca `addEventListener` sin remover.
- Eventos frecuentes (input, resize, scroll) con debounce antes de trabajo costoso.
- `@for` con `track` por id: Angular reutiliza nodos en vez de recrearlos.

## Checklist

- [ ] Landmarks, un `h1`, acciones = `<button>`, navegación = `<a>`
- [ ] Tablero/cartas con `aria-label` descriptivo; decorativos con `aria-hidden`
- [ ] Resultado/turno/ganador anunciados con `aria-live`; errores con `role="alert"`
- [ ] Todo operable por teclado con foco visible
- [ ] Contraste AA; color nunca como único indicador
- [ ] Mobile-first sin anchos fijos; probado en 3 tamaños
