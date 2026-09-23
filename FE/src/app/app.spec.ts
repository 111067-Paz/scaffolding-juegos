import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { App } from './app';
import { AuthService } from './core/auth/auth.service';

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      // Catch-all empty route so logout's navigation to /login resolves
      providers: [provideRouter([{ path: '**', children: [] }]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();
  });

  it('shows login links when there is no session', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('Ingresar');
  });

  it('shows the username and logs out through the service', async () => {
    const authService = TestBed.inject(AuthService);
    const httpTesting = TestBed.inject(HttpTestingController);
    authService.login({ username: 'lpaz', password: 'x' }).subscribe();
    httpTesting
      .expectOne('/api/auth/login')
      .flush({ token: 't', user: { id: 1, username: 'lpaz', email: 'l@p.com' } });

    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('lpaz');

    fixture.componentInstance.logout();
    httpTesting.expectOne('/api/auth/logout').flush(null);
    await fixture.whenStable();
    expect(authService.isLoggedIn()).toBe(false);
  });
});
