import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';

import { vi } from 'vitest';

import { GameNewPage, MAX_PLAYERS, MIN_PLAYERS, PLAYER_NAME_PATTERN } from './game-new-page';

describe('GameNewPage', () => {
  let page: GameNewPage;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    page = TestBed.createComponent(GameNewPage).componentInstance;
    httpTesting = TestBed.inject(HttpTestingController);
  });

  it('uses the same player-name regex as the backend (limits)', () => {
    expect(PLAYER_NAME_PATTERN.test('Ana')).toBe(true);
    expect(PLAYER_NAME_PATTERN.test('Ñandú 2')).toBe(true);
    expect(PLAYER_NAME_PATTERN.test('A')).toBe(false);
    expect(PLAYER_NAME_PATTERN.test('Beto<script>')).toBe(false);
  });

  it('keeps between MIN and MAX players', () => {
    const names = page.gameForm.controls.playerNames;
    page.removePlayer(0);
    expect(names.length).toBe(MIN_PLAYERS);
    for (let index = 0; index < 5; index++) {
      page.addPlayer();
    }
    expect(names.length).toBe(MAX_PLAYERS);
  });

  it('does not submit an invalid form and marks it touched', () => {
    page.gameForm.controls.boardSize.setValue(5);
    page.submit();
    expect(page.gameForm.touched).toBe(true);
    httpTesting.expectNone('/api/games');
  });

  it('creates the game and navigates to its board', () => {
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigate').mockResolvedValue(true);
    page.submit();
    httpTesting.expectOne('/api/games').flush({ id: 9 });
    expect(navigate).toHaveBeenCalledWith(['/games', 9]);
  });

  it('shows the backend error and re-enables the form', () => {
    page.submit();
    httpTesting
      .expectOne('/api/games')
      .flush({ status: 400, message: 'A game needs between 2 and 4 players' }, { status: 400, statusText: 'Bad Request' });
    expect(page.errorMessage()).toBe('A game needs between 2 and 4 players');
    expect(page.submitting()).toBe(false);
  });
});
