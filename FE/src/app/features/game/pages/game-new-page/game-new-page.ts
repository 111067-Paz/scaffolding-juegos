import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormArray, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { GameService } from '../../data-access/game.service';

// IDENTICAL to the backend GameCreateRequest @Pattern.
export const PLAYER_NAME_PATTERN = /^[A-Za-zÁÉÍÓÚáéíóúÑñ0-9 ]{2,30}$/;
export const MIN_PLAYERS = 2;
export const MAX_PLAYERS = 4;

function playerNameControl(value = ''): FormControl<string> {
  return new FormControl(value, {
    nonNullable: true,
    validators: [Validators.required, Validators.pattern(PLAYER_NAME_PATTERN)],
  });
}

/** Smart page: typed Reactive Form with a FormArray of 2-4 hot-seat players. */
@Component({
  selector: 'app-game-new-page',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './game-new-page.html',
})
export class GameNewPage {
  private readonly gameService = inject(GameService);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  readonly submitting = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly maxPlayers = MAX_PLAYERS;
  readonly minPlayers = MIN_PLAYERS;

  readonly gameForm = new FormGroup({
    playerNames: new FormArray([playerNameControl('Jugador 1'), playerNameControl('Jugador 2')]),
    boardSize: new FormControl(30, {
      nonNullable: true,
      validators: [Validators.required, Validators.min(10), Validators.max(60)],
    }),
  });

  addPlayer(): void {
    if (this.gameForm.controls.playerNames.length < MAX_PLAYERS) {
      this.gameForm.controls.playerNames.push(playerNameControl());
    }
  }

  removePlayer(index: number): void {
    if (this.gameForm.controls.playerNames.length > MIN_PLAYERS) {
      this.gameForm.controls.playerNames.removeAt(index);
    }
  }

  submit(): void {
    if (this.gameForm.invalid) {
      this.gameForm.markAllAsTouched();
      return;
    }
    const { playerNames, boardSize } = this.gameForm.getRawValue();
    this.submitting.set(true);
    this.errorMessage.set(null);

    this.gameService
      .create({ player_names: playerNames, board_size: boardSize })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (game) => void this.router.navigate(['/games', game.id]),
        error: (error: Error) => {
          this.errorMessage.set(error.message);
          this.submitting.set(false);
        },
      });
  }
}
