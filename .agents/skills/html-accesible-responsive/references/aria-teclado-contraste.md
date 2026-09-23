# ARIA, teclado y contraste

> Referencia de la skill html-accesible-responsive (casos reales).

## 2. ARIA — solo cuando el HTML nativo no alcanza

**Primera regla de ARIA: no usar ARIA** si existe un elemento nativo que ya lo dice.

### ❌ MAL — caso real: roles redundantes

```html
<main role="main">          <!-- ❌ <main> YA tiene role main -->
<header role="banner">      <!-- ❌ redundante -->
<footer role="contentinfo"> <!-- ❌ redundante -->
```

No rompe nada, pero es ruido que demuestra no entender qué hace ARIA. El rol viene CON la
etiqueta semántica.

### ✅ BIEN — ARIA donde agrega información real (casos reales del proyecto)

```html
<!-- SVG decorativo: ocultarlo del lector de pantalla -->
<svg aria-hidden="true" class="w-20 h-20" viewBox="0 0 100 100">...</svg>

<!-- sección titulada por su heading -->
<section aria-labelledby="error-heading">
  <h1 id="error-heading">LOST ZONE</h1>

<!-- botón cuyo texto visible no alcanza para entender la acción -->
<button aria-label="Go back to the previous page">GO BACK</button>

<!-- mensaje de error de formulario: anunciarlo -->
<p role="alert">Invalid email format</p>
```

Patrón dropdown/listbox custom (cuando un `<select>` nativo no alcanza):

```html
<button aria-haspopup="listbox" [attr.aria-expanded]="isOpen()">Supertype</button>
<ul role="listbox" aria-label="Supertype options">
  <li role="option" [attr.aria-selected]="isSelected(option)">{{ option }}</li>
</ul>
```

Y si el componente es custom, el TECLADO es tu responsabilidad (sección 3).

---

## 3. Navegación por teclado

Todo lo clickeable debe poder operarse SIN mouse:

- Tab recorre los interactivos en orden lógico (el orden del DOM — no romperlo con CSS).
- Enter/Espacio activan botones.
- Componentes custom (dropdowns): ArrowUp/ArrowDown mueven el foco, Home/End van a los
  extremos, Escape cierra.
- El foco debe ser VISIBLE: nunca `outline: none` sin reemplazo (`focus-visible:ring-2` en Tailwind).

### ✅ BIEN — caso real del proyecto (dropdown WAI-ARIA listbox)

```typescript
private handleDropdownKeydown(event: KeyboardEvent, totalOptions: number, ...): void {
  switch (event.key) {
    case 'ArrowDown':
      event.preventDefault();                      // no scrollear la página
      focusedIndex.set((focusedIndex() + 1) % totalOptions);   // wrap circular
      focusFn(nextIndex);
      break;
    case 'Home': /* primer opción */ break;
    case 'End':  /* última opción */ break;
    case 'Escape': closeFn(); break;
  }
}
```

### ⚠️ Cuidado — caso real: redirección automática temporizada

```html
<span>Automatic redirection in {{ countdown() }} seconds...</span>
```

WCAG 2.2.1 (Timing Adjustable): un límite de tiempo que el usuario no puede pausar ni extender
es una barrera (lectores de pantalla y usuarios de teclado son más lentos). Si hay countdown,
ofrecer botón para CANCELAR la redirección, o que la acción manual (GO BACK) sea la principal
y el timer generoso (≥ 20s).

---

## 4. Contraste y percepción

- Texto normal: contraste mínimo **4.5:1** (WCAG AA). Texto grande (≥ 24px): 3:1.
- Verificar con DevTools (inspector de contraste) — los grises decorativos tipo
  `text-slate-600` sobre fondo oscuro suelen fallar.
- El color NUNCA es el único indicador: error de formulario = color + ícono + texto.
- `prefers-reduced-motion`: si hay animaciones fuertes, respetarlo
  (`motion-reduce:animate-none` en Tailwind).
- Imágenes: `alt` descriptivo si informan, `alt=""` si son decorativas.

---
