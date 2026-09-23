import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';

import { vi } from 'vitest';

import { AuthService } from '../auth/auth.service';
import { LoggerService } from '../logger/logger.service';
import { errorInterceptor } from './error.interceptor';

describe('errorInterceptor', () => {
  let http: HttpClient;
  let httpTesting: HttpTestingController;
  let router: Router;
  let authService: AuthService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        provideHttpClient(withInterceptors([errorInterceptor])),
        provideHttpClientTesting(),
        { provide: LoggerService, useValue: { warn: vi.fn() } },
      ],
    });
    http = TestBed.inject(HttpClient);
    httpTesting = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    authService = TestBed.inject(AuthService);
    vi.spyOn(router, 'navigate').mockResolvedValue(true);
  });

  it('a 401 on a protected call clears the session and goes to /login, re-throwing the error', () => {
    let status = 0;
    http.get('/api/games').subscribe({ error: (error: { status: number }) => (status = error.status) });
    httpTesting.expectOne('/api/games').flush(null, { status: 401, statusText: 'Unauthorized' });

    expect(status).toBe(401);
    expect(authService.sessionStatus()).toBe('anonymous');
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('a 401 on an auth call (bad login) does not redirect', () => {
    http.post('/api/auth/login', {}).subscribe({ error: () => undefined });
    httpTesting.expectOne('/api/auth/login').flush(null, { status: 401, statusText: 'Unauthorized' });
    expect(router.navigate).not.toHaveBeenCalled();
  });

  it('other errors are only re-thrown', () => {
    let status = 0;
    http.get('/api/games/1').subscribe({ error: (error: { status: number }) => (status = error.status) });
    httpTesting.expectOne('/api/games/1').flush(null, { status: 404, statusText: 'Not Found' });
    expect(status).toBe(404);
    expect(router.navigate).not.toHaveBeenCalled();
  });
});
