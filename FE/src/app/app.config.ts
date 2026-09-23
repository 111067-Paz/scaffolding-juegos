import { provideHttpClient, withFetch, withInterceptors } from '@angular/common/http';
import {
  ApplicationConfig,
  provideBrowserGlobalErrorListeners,
  provideZonelessChangeDetection,
} from '@angular/core';
import { provideRouter, withComponentInputBinding } from '@angular/router';

import { routes } from './app.routes';
import { errorInterceptor } from './core/http/error.interceptor';

/**
 * Root providers.
 * - Zoneless (Angular 21 default, made explicit): the view only updates through signals.
 * - withComponentInputBinding: route params arrive as component input() values.
 * - withFetch: native fetch API; same-origin requests carry the HttpOnly auth cookie.
 * - No global LOCALE_ID / date config: each date declares its format (fechas skill).
 */
export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZonelessChangeDetection(),
    provideRouter(routes, withComponentInputBinding()),
    provideHttpClient(withFetch(), withInterceptors([errorInterceptor])),
  ],
};
