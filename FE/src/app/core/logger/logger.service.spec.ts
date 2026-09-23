import { LoggerService } from './logger.service';

describe('LoggerService', () => {
  const logger = new LoggerService();

  it('masks sensitive keys at any depth and keeps the rest', () => {
    const sanitized = logger.sanitize({
      username: 'lpaz',
      password: 'secret1',
      nested: { token: 'jwt', list: [{ Secret: 's' }] },
    });
    expect(sanitized).toEqual({
      username: 'lpaz',
      password: '***',
      nested: { token: '***', list: [{ Secret: '***' }] },
    });
  });

  it('passes primitives through', () => {
    expect(logger.sanitize(42)).toBe(42);
    expect(logger.sanitize(null)).toBeNull();
  });
});
