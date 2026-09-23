import { TestBed } from '@angular/core/testing';

import { DicePanel } from './dice-panel';

describe('DicePanel', () => {
  it('shows the player and last value, and emits roll on click', async () => {
    const fixture = TestBed.createComponent(DicePanel);
    fixture.componentRef.setInput('playerName', 'Ana');
    fixture.componentRef.setInput('lastValue', 5);
    let emitted = 0;
    fixture.componentInstance.roll.subscribe(() => emitted++);
    await fixture.whenStable();

    const element = fixture.nativeElement as HTMLElement;
    expect(element.textContent).toContain('Ana');
    expect(element.querySelector('[aria-live]')?.textContent?.trim()).toBe('5');
    element.querySelector('button')?.click();
    expect(emitted).toBe(1);
  });

  it('disables the button while busy', async () => {
    const fixture = TestBed.createComponent(DicePanel);
    fixture.componentRef.setInput('playerName', 'Ana');
    fixture.componentRef.setInput('disabled', true);
    await fixture.whenStable();
    expect((fixture.nativeElement as HTMLElement).querySelector('button')?.disabled).toBe(true);
  });
});
