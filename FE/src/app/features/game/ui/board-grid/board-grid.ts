import { Component, computed, input, ChangeDetectionStrategy } from '@angular/core';

import { CELL_META, Cell, Player } from '../../data-access/game.models';
import { PLAYER_COLORS } from '../player-colors';

interface CellView {
  position: number;
  label: string;
  icon: string;
  css: string;
  players: Player[];
  ariaLabel: string;
}

/**
 * Dumb component: draws the board from inputs only (no services, no HTTP).
 * Everything the template needs is precomputed in a computed() — the template
 * never calls methods.
 */
@Component({
  selector: 'app-board-grid',
  changeDetection: ChangeDetectionStrategy.Eager,
  templateUrl: './board-grid.html',
})
export class BoardGrid {
  readonly cells = input.required<Cell[]>();
  readonly players = input.required<Player[]>();

  readonly colors = PLAYER_COLORS;

  readonly cellViews = computed<CellView[]>(() =>
    this.cells().map((cell) => {
      const meta = CELL_META[cell.type];
      const here = this.players().filter((player) => player.position === cell.position);
      const names = here.map((player) => player.name).join(', ');
      return {
        position: cell.position,
        label: meta.label,
        icon: meta.icon,
        css: meta.css,
        players: here,
        ariaLabel: `Casilla ${cell.position}: ${meta.label}${names ? `. Jugadores: ${names}` : ''}`,
      };
    }),
  );
}
