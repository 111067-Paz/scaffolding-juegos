import { Component, input, ChangeDetectionStrategy } from '@angular/core';

import { CELL_META, Move } from '../../data-access/game.models';

/** Dumb component: move history. Dates are shown exactly as the backend formatted them. */
@Component({
  selector: 'app-move-history',
  changeDetection: ChangeDetectionStrategy.Eager,
  template: `
    <ol
      class="flex max-h-72 flex-col gap-1 overflow-y-auto text-sm"
      aria-label="Historial de movimientos"
    >
      @for (move of moves(); track move.id) {
        <li class="rounded bg-white px-3 py-1.5">
          <strong>{{ move.player_name }}</strong> sacó {{ move.dice_value }}:
          {{ move.from_position }} → {{ move.to_position }}
          <span class="text-slate-500">({{ cellMeta[move.cell_type].label }})</span>
          <span class="block text-xs text-slate-400">{{ move.created_at }}</span>
        </li>
      } @empty {
        <li class="text-slate-500">Todavía no hay movimientos.</li>
      }
    </ol>
  `,
})
export class MoveHistory {
  readonly moves = input.required<Move[]>();

  readonly cellMeta = CELL_META;
}
