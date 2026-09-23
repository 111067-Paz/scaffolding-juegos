import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';

import { catchError, throwError } from 'rxjs';

import { AuthService } from '../auth/auth.service';
import { LoggerService } from '../logger/logger.service';

/**
 * Cross-cutting HTTP error handling (functional interceptor):
 * - 401 on a protected call → the session expired: clear it and go to /login.
 * - every failure is logged (sanitized) through LoggerService.
 * The error is re-thrown so each service/resource still decides what to show.
 */
export const errorInterceptor: HttpInterceptorFn = (request, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const logger = inject(LoggerService);

  return next(request).pipe(
    catchError((error: unknown) => {
      if (error instanceof HttpErrorResponse) {
        logger.warn(`HTTP ${error.status} on ${request.method} ${request.url}`, error.error);
        const isAuthCall = request.url.includes('/auth/');
        if (error.status === 401 && !isAuthCall) {
          authService.clearSession();
          void router.navigate(['/login']);
        }
      }
      return throwError(() => error);
    }),
  );
};
