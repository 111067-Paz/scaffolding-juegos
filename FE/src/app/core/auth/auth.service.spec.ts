import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;
  let httpTesting: HttpTestingController;
  const user = { id: 1, username: 'lpaz', email: 'lpaz@utn.edu.ar' };

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(AuthService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTesting.verify());

  it('starts with an unknown session', () => {
    expect(service.sessionStatus()).toBe('unknown');
    expect(service.isLoggedIn()).toBe(false);
  });

  it('login opens the session with the user and never keeps the token', () => {
    service.login({ username: 'lpaz', password: 'Secret123' }).subscribe();
    httpTesting.expectOne({ method: 'POST', url: '/api/auth/login' }).flush({ token: 'jwt', user });

    expect(service.isLoggedIn()).toBe(true);
    expect(service.currentUser()).toEqual(user);
    expect(JSON.stringify(localStorage)).not.toContain('jwt');
  });

  it('login failure surfaces the backend message', () => {
    let message = '';
    service.login({ username: 'lpaz', password: 'bad' }).subscribe({ error: (error: Error) => (message = error.message) });
    httpTesting
      .expectOne('/api/auth/login')
      .flush({ status: 401, message: 'Invalid username or password' }, { status: 401, statusText: 'Unauthorized' });

    expect(message).toBe('Invalid username or password');
    expect(service.isLoggedIn()).toBe(false);
  });

  it('register opens the session', () => {
    service.register({ username: 'lpaz', email: 'lpaz@utn.edu.ar', password: 'Secret123' }).subscribe();
    httpTesting.expectOne({ method: 'POST', url: '/api/auth/register' }).flush({ token: 'jwt', user });
    expect(service.isLoggedIn()).toBe(true);
  });

  it('register failure surfaces the backend message', () => {
    let message = '';
    service
      .register({ username: 'lpaz', email: 'lpaz@utn.edu.ar', password: 'Secret123' })
      .subscribe({ error: (error: Error) => (message = error.message) });
    httpTesting
      .expectOne('/api/auth/register')
      .flush({ status: 409, message: 'already exists' }, { status: 409, statusText: 'Conflict' });
    expect(message).toBe('already exists');
  });

  it('restoreSession emits true when the cookie is valid', () => {
    let restored: boolean | undefined;
    service.restoreSession().subscribe((value) => (restored = value));
    httpTesting.expectOne('/api/auth/me').flush(user);

    expect(restored).toBe(true);
    expect(service.sessionStatus()).toBe('authenticated');
  });

  it('restoreSession emits false (no error) when there is no session', () => {
    let restored: boolean | undefined;
    service.restoreSession().subscribe((value) => (restored = value));
    httpTesting.expectOne('/api/auth/me').flush(null, { status: 401, statusText: 'Unauthorized' });

    expect(restored).toBe(false);
    expect(service.sessionStatus()).toBe('anonymous');
  });

  it('logout clears the session even if the request fails', () => {
    service.logout().subscribe();
    httpTesting.expectOne('/api/auth/logout').flush(null, { status: 500, statusText: 'Error' });
    expect(service.sessionStatus()).toBe('anonymous');
  });
});
