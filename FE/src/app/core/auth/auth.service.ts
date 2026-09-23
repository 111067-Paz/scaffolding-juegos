import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';

import { Observable, catchError, map, of, tap, throwError } from 'rxjs';

import { environment } from '../../../environments/environment';
import { extractErrorMessage } from '../http/api-error';
import { AuthResponse, AuthUser, LoginRequest, RegisterRequest, SessionStatus } from './auth.models';

/**
 * Session state with signals. The JWT lives in an HttpOnly cookie set by the
 * backend: JavaScript never reads or stores it (no localStorage/sessionStorage).
 * Same-origin requests (dev proxy / Nginx) send the cookie automatically.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly authUrl = `${environment.apiUrl}/auth`;

  private readonly user = signal<AuthUser | null>(null);
  private readonly status = signal<SessionStatus>('unknown');

  readonly currentUser = this.user.asReadonly();
  readonly sessionStatus = this.status.asReadonly();
  readonly isLoggedIn = computed(() => this.status() === 'authenticated');

  login(request: LoginRequest): Observable<AuthUser> {
    return this.http.post<AuthResponse>(`${this.authUrl}/login`, request).pipe(
      map((response) => response.user),
      tap((user) => this.startSession(user)),
      catchError((error: unknown) => throwError(() => new Error(extractErrorMessage(error)))),
    );
  }

  register(request: RegisterRequest): Observable<AuthUser> {
    return this.http.post<AuthResponse>(`${this.authUrl}/register`, request).pipe(
      map((response) => response.user),
      tap((user) => this.startSession(user)),
      catchError((error: unknown) => throwError(() => new Error(extractErrorMessage(error)))),
    );
  }

  /**
   * Asks the backend who owns the cookie (after a page reload).
   * Emits true/false and never errors: "no session" is a normal outcome.
   */
  restoreSession(): Observable<boolean> {
    return this.http.get<AuthUser>(`${this.authUrl}/me`).pipe(
      tap((user) => this.startSession(user)),
      map(() => true),
      catchError(() => {
        this.clearSession();
        return of(false);
      }),
    );
  }

  /** Deletes the cookie server-side; the local session is cleared even if the call fails. */
  logout(): Observable<void> {
    return this.http.post<void>(`${this.authUrl}/logout`, {}).pipe(
      catchError(() => of(undefined)),
      tap(() => this.clearSession()),
    );
  }

  clearSession(): void {
    this.user.set(null);
    this.status.set('anonymous');
  }

  private startSession(user: AuthUser): void {
    this.user.set(user);
    this.status.set('authenticated');
  }
}
