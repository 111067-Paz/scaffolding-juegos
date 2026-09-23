import { Component, computed, inject } from '@angular/core';
import { RouterLink } from '@angular/router';

import { extractErrorMessage } from '../../../../core/http/api-error';
import { STATUS_LABEL } from '../../data-access/game.models';
import { GameService } from '../../data-access/game.service';

/** Smart page: "my games". Data via httpResource — no manual subscribe, no ngOnInit. */
@Component({
  selector: 'app-game-list-page',
  imports: [RouterLink],
  templateUrl: './game-list-page.html',
})
export class GameListPage {
  private readonly gameService = inject(GameService);

  readonly games = this.gameService.myGamesResource();
  readonly errorMessage = computed(() => (this.games.error() ? extractErrorMessage(this.games.error()) : null));
  readonly statusLabel = STATUS_LABEL;
}
