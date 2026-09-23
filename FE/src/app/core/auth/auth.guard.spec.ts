import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot, UrlTree, provideRouter } from '@angular/router';

import { Observable } from 'rxjs';

import { authGuard } from './auth.guard';
import { AuthService } from './auth.service';

describe('authGuard', () => {
  let httpTesting: HttpTestingController;
  let authService: AuthService;

  const runGuard = (): ReturnType<typeof authGuard> =>
    TestBed.runInInjectionContext(() =>
      authGuard({} as ActivatedRouteSnapshot, {} as RouterStateSnapshot),
    );

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    });
    httpTesting = TestBed.inject(HttpTestingController);
    authService = TestBed.inject(AuthService);
  });

  it('asks /me on the first navigation and lets an authenticated user in', () => {
    let decision: boolean | UrlTree | undefined;
    (runGuard() as Observable<boolean | UrlTree>).subscribe((value) => (decision = value));
    httpTesting.expectOne('/api/auth/me').flush({ id: 1, username: 'lpaz', email: 'l@p.com' });
    expect(decision).toBe(true);
  });

  it('redirects to /login when /me says there is no session', () => {
    let decision: boolean | UrlTree | undefined;
    (runGuard() as Observable<boolean | UrlTree>).subscribe((value) => (decision = value));
    httpTesting.expectOne('/api/auth/me').flush(null, { status: 401, statusText: 'Unauthorized' });
    expect(TestBed.inject(Router).serializeUrl(decision as UrlTree)).toBe('/login');
  });

  it('decides synchronously once the session status is known', () => {
    authService.clearSession();
    const decision = runGuard() as UrlTree;
    expect(TestBed.inject(Router).serializeUrl(decision)).toBe('/login');
    httpTesting.expectNone('/api/auth/me');
  });
});
