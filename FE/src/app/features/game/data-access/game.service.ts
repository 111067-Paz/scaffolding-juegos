import { HttpClient, HttpResourceRef, httpResource } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';

import { Observable, catchError, throwError } from 'rxjs';

import { environment } from '../../../../environments/environment';
import { extractErrorMessage } from '../../../core/http/api-error';
import { Game, GameCreateRequest, GameSummary, Move, RollResult } from './game.models';

/**
 * The ONLY class of the feature that talks HTTP (components never touch HttpClient).
 * - Queries (GET) → httpResource: signal-based (value/isLoading/error/reload),
 *   re-fetches by itself when the signals read in the URL function change.
 *   Must be called from an injection context (a component field initializer).
 * - Commands (POST) → Observables, triggered by user actions.
 */
@Injectable({ providedIn: 'root' })
export class GameService {
  private readonly http = inject(HttpClient);
  private readonly gamesUrl = `${environment.apiUrl}/games`;

  myGamesResource(): HttpResourceRef<GameSummary[] | undefined> {
    return httpResource<GameSummary[]>(() => this.gamesUrl);
  }

  /** Returning undefined from the URL function means "do not fetch yet". */
  gameResource(gameId: () => number | undefined): HttpResourceRef<Game | undefined> {
    return httpResource<Game>(() => {
      const id = gameId();
      return id === undefined ? undefined : `${this.gamesUrl}/${id}`;
    });
  }

  movesResource(gameId: () => number | undefined): HttpResourceRef<Move[] | undefined> {
    return httpResource<Move[]>(() => {
      const id = gameId();
      return id === undefined ? undefined : `${this.gamesUrl}/${id}/moves`;
    });
  }

  create(request: GameCreateRequest): Observable<Game> {
    return this.http.post<Game>(this.gamesUrl, request).pipe(catchError(toUserError));
  }

  start(gameId: number): Observable<Game> {
    return this.http.post<Game>(`${this.gamesUrl}/${gameId}/start`, {}).pipe(catchError(toUserError));
  }

  roll(gameId: number, playerId: number): Observable<RollResult> {
    return this.http
      .post<RollResult>(`${this.gamesUrl}/${gameId}/roll`, { player_id: playerId })
      .pipe(catchError(toUserError));
  }
}

function toUserError(error: unknown): Observable<never> {
  return throwError(() => new Error(extractErrorMessage(error)));
}
