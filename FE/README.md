# Scaffolding Frontend — Parcial Programación III

Angular 21 · TypeScript · Tailwind CSS 4 · SPA con routing lazy

## Arranque rápido

```bash
npm install     # solo la primera vez
npm start       # ng serve con proxy a http://localhost:8080 (backend)
```

App en http://localhost:4200 — las llamadas a `/api/**` van al backend via `proxy.conf.json`
(sin CORS). Levantá el BE primero.

## Estructura

```
src/app/
├── components/     product-card — DUMB: @Input/@Output, sin servicios de datos
├── pages/          product-list (SMART: signals + computed + búsqueda con debounce),
│                   product-form (reactive form tipado), not-found
├── services/       product.service — HTTP tipado, catchError, HttpErrorResponse
├── models/         product.model — fechas como string ISO (nunca Date)
├── guards/         auth.guard — funcional (CanActivateFn)
├── pipes/          truncate.pipe — pipe puro de ejemplo
├── app.config.ts   provideHttpClient + locale es-AR registrado
└── app.routes.ts   TODAS las rutas lazy (loadComponent), wildcard al final
```

## El día del parcial

1. Copiá esta carpeta y `npm install`.
2. Renombrá la feature `product` al dominio del enunciado: model → service → pages →
   componente dumb. Las rutas en `app.routes.ts`.
3. Reglas (ver skills): OnPush siempre, sin `standalone: true` explícito, `@if/@for/@switch`
   con `track` por id, bindings `[class.x]` (no ngClass), signals + `computed()` (no métodos
   en el template), cero `any`, HTTP solo en servicios, formularios reactivos con errores
   visibles tras `touched`.
4. Fechas: el modelo guarda el string ISO; presentar SOLO con `| date:'dd/MM/yyyy HH:mm'`
   (locale es-AR ya registrado en `app.config.ts`).
5. Responsive: mobile-first con prefijos `md:`/`lg:`; probar en 375/768/1280.

## Tests

```bash
npm test
```
