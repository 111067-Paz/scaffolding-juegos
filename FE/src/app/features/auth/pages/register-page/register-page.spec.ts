import { FormControl, FormGroup } from '@angular/forms';

import { EMAIL_PATTERN, PASSWORD_PATTERN, USERNAME_PATTERN, passwordsMatchValidator } from './register-page';

describe('RegisterPage validation contract', () => {
  it('regex mirror the backend RegisterRequest', () => {
    expect(USERNAME_PATTERN.test('lpaz_1')).toBe(true);
    expect(USERNAME_PATTERN.test('1lpaz')).toBe(false);
    expect(EMAIL_PATTERN.test('a@b.com')).toBe(true);
    expect(EMAIL_PATTERN.test('a@b')).toBe(false);
    expect(PASSWORD_PATTERN.test('Secret123')).toBe(true);
    expect(PASSWORD_PATTERN.test('secret123')).toBe(false);
  });

  it('passwordsMatchValidator works at FormGroup level', () => {
    const group = new FormGroup({ password: new FormControl('Abc12345'), confirmPassword: new FormControl('Abc12345') });
    expect(passwordsMatchValidator(group)).toBeNull();
    group.controls.confirmPassword.setValue('other');
    expect(passwordsMatchValidator(group)).toEqual({ passwordsMismatch: true });
  });
});
