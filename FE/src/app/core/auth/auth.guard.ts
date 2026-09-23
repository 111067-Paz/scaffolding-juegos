import { inject } from '@angular/core';
import { CanActivateFn, Router, UrlTree } from '@angular/router';

import { Observable, map } from 'rxjs';

import { AuthService } from './auth.service';

/**
 * Functional guard. The guard is UX, not security: the backend validates the
 * token on every request anyway. On the first navigation the session status is
 * unknown, so it asks /api/auth/me once before deciding.
 */
export const authGuard: CanActivateFn = (): boolean | UrlTree | Observable<boolean | UrlTree> => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const loginTree = router.createUrlTree(['/login']);

  if (authService.sessionStatus() === 'unknown') {
    return authService.restoreSession().pipe(map((loggedIn) => (loggedIn ? true : loginTree)));
  }
  return authService.isLoggedIn() ? true : loginTree;
};
