import { TestBed } from '@angular/core/testing';

import { Cell, Player } from '../../data-access/game.models';
import { BoardGrid } from './board-grid';

const PLAYER: Player = {
  id: 100,
  name: 'Ana',
  turn_order: 0,
  position: 2,
  lives: 3,
  skip_next_turn: false,
  alive: true,
};

describe('BoardGrid', () => {
  it('renders one cell per position with an accessible label that names the players on it', async () => {
    const cells: Cell[] = [
      { position: 0, type: 'NORMAL' },
      { position: 1, type: 'TRAP' },
      { position: 2, type: 'GOAL' },
    ];
    const fixture = TestBed.createComponent(BoardGrid);
    fixture.componentRef.setInput('cells', cells);
    fixture.componentRef.setInput('players', [PLAYER]);
    await fixture.whenStable();

    const items = (fixture.nativeElement as HTMLElement).querySelectorAll('li');
    expect(items.length).toBe(3);
    expect(items[1].getAttribute('aria-label')).toBe('Casilla 1: Trampa: −1 vida');
    expect(items[2].getAttribute('aria-label')).toBe('Casilla 2: Meta. Jugadores: Ana');
  });

  it('shows the empty state without cells', async () => {
    const fixture = TestBed.createComponent(BoardGrid);
    fixture.componentRef.setInput('cells', []);
    fixture.componentRef.setInput('players', []);
    await fixture.whenStable();
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('vacío');
  });
});
