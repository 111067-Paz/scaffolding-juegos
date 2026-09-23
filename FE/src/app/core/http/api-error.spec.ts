import { HttpErrorResponse } from '@angular/common/http';

import { extractErrorMessage } from './api-error';

describe('extractErrorMessage', () => {
  it('returns the backend ErrorApi message', () => {
    const error = new HttpErrorResponse({ status: 409, error: { status: 409, message: 'Not your turn' } });
    expect(extractErrorMessage(error)).toBe('Not your turn');
  });

  it('explains a network failure (status 0)', () => {
    expect(extractErrorMessage(new HttpErrorResponse({ status: 0 }))).toContain('conectar');
  });

  it('falls back to a generic message for unknown shapes', () => {
    expect(extractErrorMessage(new HttpErrorResponse({ status: 500, error: 'boom' }))).toContain('inesperado');
    expect(extractErrorMessage(new Error('x'))).toContain('inesperado');
  });
});
