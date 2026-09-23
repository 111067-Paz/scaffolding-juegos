import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ApplicationRef, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';

import { Game } from './game.models';
import { GameService } from './game.service';

const GAME: Game = {
  id: 7,
  status: 'WAITING',
  board_size: 10,
  current_player_id: null,
  winner_id: null,
  players: [],
  cells: [],
  created_at: '17-09-2026 21:30:00',
};

describe('GameService', () => {
  let service: GameService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(GameService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTesting.verify());

  it('create POSTs the snake_case contract', () => {
    let created: Game | undefined;
    service.create({ player_names: ['Ana', 'Beto'], board_size: 10 }).subscribe((game) => (created = game));

    const request = httpTesting.expectOne({ method: 'POST', url: '/api/games' });
    expect(request.request.body).toEqual({ player_names: ['Ana', 'Beto'], board_size: 10 });
    request.flush(GAME);
    expect(created?.id).toBe(7);
  });

  it('start and roll hit their endpoints; roll sends player_id', () => {
    service.start(7).subscribe();
    httpTesting.expectOne({ method: 'POST', url: '/api/games/7/start' }).flush(GAME);

    service.roll(7, 100).subscribe();
    const roll = httpTesting.expectOne({ method: 'POST', url: '/api/games/7/roll' });
    expect(roll.request.body).toEqual({ player_id: 100 });
    roll.flush({ move: {}, game: GAME });
  });

  it('commands turn HTTP errors into user messages', () => {
    let message = '';
    service.roll(7, 101).subscribe({ error: (error: Error) => (message = error.message) });
    httpTesting
      .expectOne('/api/games/7/roll')
      .flush({ status: 409, message: "It is not Beto's turn" }, { status: 409, statusText: 'Conflict' });
    expect(message).toBe("It is not Beto's turn");
  });

  it('gameResource waits for an id and fetches when it appears', async () => {
    const id = signal<number | undefined>(undefined);
    const resource = TestBed.runInInjectionContext(() => service.gameResource(() => id()));

    TestBed.inject(ApplicationRef).tick();
    httpTesting.expectNone('/api/games/7');

    id.set(7);
    TestBed.inject(ApplicationRef).tick();
    httpTesting.expectOne('/api/games/7').flush(GAME);
    await TestBed.inject(ApplicationRef).whenStable();

    expect(resource.value()?.status).toBe('WAITING');
  });

  it('myGamesResource and movesResource request their URLs', async () => {
    TestBed.runInInjectionContext(() => {
      service.myGamesResource();
      service.movesResource(() => 7);
    });
    TestBed.inject(ApplicationRef).tick();
    httpTesting.expectOne('/api/games').flush([]);
    httpTesting.expectOne('/api/games/7/moves').flush([]);
    await TestBed.inject(ApplicationRef).whenStable();
  });

  it('movesResource does not fetch without id', () => {
    TestBed.runInInjectionContext(() => service.movesResource(() => undefined));
    TestBed.inject(ApplicationRef).tick();
    httpTesting.expectNone((request) => request.url.includes('/moves'));
  });
});
