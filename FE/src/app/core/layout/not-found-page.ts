import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-not-found-page',
  imports: [RouterLink],
  template: `
    <section class="mx-auto max-w-md px-4 py-16 text-center" aria-labelledby="not-found-heading">
      <h1 id="not-found-heading" class="mb-2 text-3xl font-bold text-slate-900">404</h1>
      <p class="mb-6 text-slate-600">La página que buscás no existe.</p>
      <a
        routerLink="/games"
        class="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 focus-visible:ring-2 focus-visible:ring-indigo-400"
      >
        Volver a mis partidas
      </a>
    </section>
  `,
})
export class NotFoundPage {}
