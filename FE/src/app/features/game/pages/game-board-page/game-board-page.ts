import { Component, DestroyRef, computed, inject, input, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';

import { Observable } from 'rxjs';

import { extractErrorMessage } from '../../../../core/http/api-error';
import { STATUS_LABEL } from '../../data-access/game.models';
import { GameService } from '../../data-access/game.service';
import { BoardGrid } from '../../ui/board-grid/board-grid';
import { DicePanel } from '../../ui/dice-panel/dice-panel';
import { MoveHistory } from '../../ui/move-history/move-history';
import { PlayerPanel } from '../../ui/player-panel/player-panel';

/**
 * Smart page (container): owns the resources and the commands, derives every
 * view value with computed(), and passes plain data down to dumb components.
 * The route param arrives as an input thanks to withComponentInputBinding().
 */
@Component({
  selector: 'app-game-board-page',
  imports: [RouterLink, BoardGrid, DicePanel, PlayerPanel, MoveHistory],
  templateUrl: './game-board-page.html',
})
export class GameBoardPage {
  private readonly gameService = inject(GameService);
  private readonly destroyRef = inject(DestroyRef);

  readonly id = input.required<string>();
  readonly gameId = computed(() => Number(this.id()));

  readonly game = this.gameService.gameResource(() => this.gameId());
  readonly moves = this.gameService.movesResource(() => this.gameId());

  readonly busy = signal(false);
  readonly actionError = signal<string | null>(null);
  readonly lastDice = signal<number | null>(null);

  readonly statusLabel = STATUS_LABEL;
  readonly loadError = computed(() => (this.game.error() ? extractErrorMessage(this.game.error()) : null));
  readonly currentPlayer = computed(() => {
    const game = this.game.value();
    return game?.players.find((player) => player.id === game.current_player_id) ?? null;
  });
  readonly winner = computed(() => {
    const game = this.game.value();
    return game?.players.find((player) => player.id === game.winner_id) ?? null;
  });

  start(): void {
    this.runCommand(() => this.gameService.start(this.gameId()), (game) => this.game.set(game));
  }

  roll(): void {
    const player = this.currentPlayer();
    if (!player) {
      return;
    }
    this.runCommand(
      () => this.gameService.roll(this.gameId(), player.id),
      (result) => {
        this.lastDice.set(result.move.dice_value);
        // Local update with the server response: no extra GET for the game.
        this.game.set(result.game);
        this.moves.reload();
      },
    );
  }

  private runCommand<T>(command: () => Observable<T>, onSuccess: (value: T) => void): void {
    this.busy.set(true);
    this.actionError.set(null);
    command()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (value) => {
          onSuccess(value);
          this.busy.set(false);
        },
        error: (error: Error) => {
          this.actionError.set(error.message);
          this.busy.set(false);
        },
      });
  }
}
