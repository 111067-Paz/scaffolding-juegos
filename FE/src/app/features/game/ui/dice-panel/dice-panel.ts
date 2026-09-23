import { Component, input, output } from '@angular/core';

/**
 * Dumb component: shows the last die value and emits "roll" — it does not know
 * about games, services or HTTP. aria-live announces the result to screen readers.
 */
@Component({
  selector: 'app-dice-panel',
  template: `
    <section class="flex flex-col items-center gap-3 rounded-xl bg-white p-4 shadow-sm" aria-label="Dado">
      <p class="text-sm text-slate-600">
        Turno de <strong class="text-slate-900">{{ playerName() }}</strong>
      </p>
      <p class="flex size-16 items-center justify-center rounded-xl border-2 border-slate-300 text-3xl font-bold" aria-live="polite">
        {{ lastValue() ?? '–' }}
      </p>
      <button
        type="button"
        (click)="roll.emit()"
        [disabled]="disabled()"
        class="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-700 focus-visible:ring-2 focus-visible:ring-indigo-400 disabled:opacity-50"
      >
        Tirar dado
      </button>
    </section>
  `,
})
export class DicePanel {
  readonly playerName = input.required<string>();
  readonly lastValue = input<number | null>(null);
  readonly disabled = input(false);

  readonly roll = output<void>();
}
