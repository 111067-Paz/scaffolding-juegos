import { HttpErrorResponse } from '@angular/common/http';

import { ErrorApi } from '../models/error-api.model';

const FALLBACK_MESSAGE = 'Ocurrió un error inesperado. Intentá de nuevo.';

function isErrorApi(body: unknown): body is ErrorApi {
  return typeof body === 'object' && body !== null && 'message' in body;
}

/**
 * Turns any HTTP failure into a user-facing message.
 * Type guard instead of `any`: the body is `unknown` until proven to be ErrorApi.
 */
export function extractErrorMessage(error: unknown): string {
  if (error instanceof HttpErrorResponse) {
    if (error.status === 0) {
      return 'No se pudo conectar con el servidor.';
    }
    if (isErrorApi(error.error)) {
      return error.error.message;
    }
  }
  return FALLBACK_MESSAGE;
}
