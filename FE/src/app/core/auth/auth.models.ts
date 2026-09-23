/**
 * Mirrors the backend auth DTOs. The token also arrives in the body (for API
 * clients) but the SPA IGNORES it: the session lives in the HttpOnly cookie.
 */
export interface AuthUser {
  id: number;
  username: string;
  email: string;
}

export interface AuthResponse {
  token: string;
  user: AuthUser;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export type SessionStatus = 'unknown' | 'authenticated' | 'anonymous';
