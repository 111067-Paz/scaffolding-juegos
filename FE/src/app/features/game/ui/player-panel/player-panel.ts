import { Component, input, ChangeDetectionStrategy } from '@angular/core';

import { Player } from '../../data-access/game.models';
import { PLAYER_COLORS } from '../player-colors';

/** Dumb component: players with position, lives and whose turn it is. */
@Component({
  selector: 'app-player-panel',
  changeDetection: ChangeDetectionStrategy.Eager,
  template: `
    <ul class="flex flex-col gap-2" aria-label="Jugadores">
      @for (player of players(); track player.id) {
        <li
          class="flex items-center justify-between rounded-lg border bg-white px-3 py-2"
          [class.border-indigo-500]="player.id === currentPlayerId()"
          [class.border-slate-200]="player.id !== currentPlayerId()"
          [class.opacity-50]="!player.alive"
          [attr.aria-current]="player.id === currentPlayerId() ? 'step' : null"
        >
          <span class="flex items-center gap-2">
            <span
              class="size-3 rounded-full"
              [class]="colors[player.turn_order]"
              aria-hidden="true"
            ></span>
            <span class="font-medium">{{ player.name }}</span>
            @if (player.id === winnerId()) {
              <span class="rounded bg-indigo-100 px-1.5 text-xs text-indigo-700">Ganador</span>
            }
            @if (player.skip_next_turn) {
              <span class="rounded bg-sky-100 px-1.5 text-xs text-sky-700">Pierde turno</span>
            }
          </span>
          <span class="text-sm text-slate-600">
            Casilla {{ player.position }} ·
            <span [attr.aria-label]="player.lives + ' vidas'">{{
              player.alive ? '♥'.repeat(player.lives) : 'Eliminado'
            }}</span>
          </span>
        </li>
      } @empty {
        <li class="text-slate-500">Sin jugadores.</li>
      }
    </ul>
  `,
})
export class PlayerPanel {
  readonly players = input.required<Player[]>();
  readonly currentPlayerId = input<number | null>(null);
  readonly winnerId = input<number | null>(null);

  readonly colors = PLAYER_COLORS;
}
