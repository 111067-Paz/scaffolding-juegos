# Formularios reactivos tipados

> Referencia de la skill angular-spa. En el scaffold: register-page (validador cross-field) y game-new-page (FormArray). En templates usar @let en vez de métodos helper.

## 10. Formularios reactivos

**SIEMPRE Reactive Forms. Template-driven está DEPRECADO** (confirmado por la cátedra: se busca
lo escalable y las buenas prácticas). `FormsModule` + `[(ngModel)]` solo se toca si un enunciado
viejo lo exige textualmente — nunca por elección. La validación vive en TypeScript, tipada y
testeable, no esparcida en atributos del HTML.

```typescript
readonly filterForm = new FormGroup({
  name: new FormControl('', { nonNullable: true }),
  email: new FormControl('', { nonNullable: true,
      validators: [Validators.required, Validators.email] })
});

submit(): void {
  if (this.filterForm.invalid) {
    this.filterForm.markAllAsTouched();   // dispara los mensajes de error visibles
    return;
  }
  // usar this.filterForm.getRawValue()
}
```

```html
<form [formGroup]="filterForm" (ngSubmit)="submit()">
  <input formControlName="email" type="email" />
  @if (filterForm.controls.email.touched && filterForm.controls.email.hasError('email')) {
    <p role="alert">Invalid email format</p>
  }
</form>
```

### Validación robusta con `Validators.pattern`

> ⚠️ `Validators.email` es LAXO: acepta `asad@sa` y `321@123` (no exige TLD). Para validación
> seria usá `Validators.pattern` con un regex explícito. **El MISMO regex debe estar en el
> backend** (`@Pattern` / `@Email(regexp=...)`): el contrato de validación tiene que coincidir
> en ambas capas — el back NUNCA confía solo en el cliente.

```typescript
// Regex IDÉNTICOS a los del backend (RegisterRequest). Definirlos como constantes.
private static readonly USERNAME_PATTERN = /^[a-zA-Z][a-zA-Z0-9_]*$/;       // empieza con letra
private static readonly EMAIL_PATTERN = /^[\w.%+-]+@[\w.-]+\.[A-Za-z]{2,}$/; // exige dominio.tld
private static readonly PASSWORD_PATTERN = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).+$/; // mayús+minús+número

username: new FormControl('', { nonNullable: true, validators: [
  Validators.required, Validators.minLength(3), Validators.maxLength(50),
  Validators.pattern(RegisterPage.USERNAME_PATTERN)   // longitud y formato separados = mensajes claros
] })
```

En el HTML, distinguí cada error para dar un mensaje específico (longitud vs formato):

```html
@if (form.controls.username.hasError('required')) { Username is required. }
@else if (form.controls.username.hasError('minlength')
       || form.controls.username.hasError('maxlength')) { Must be 3–50 characters. }
@else { Must start with a letter — only letters, numbers or underscore. }
```

### Validador cross-field (a nivel `FormGroup`)

Para comparar DOS controles (ej. password === confirmPassword) el validador va en el
**`FormGroup`**, no en un `FormControl` (un control solo se ve a sí mismo):

```typescript
function passwordsMatchValidator(group: AbstractControl): ValidationErrors | null {
  const password = group.get('password')?.value;
  const confirmPassword = group.get('confirmPassword')?.value;
  return password === confirmPassword ? null : { passwordsMismatch: true };
}

readonly registerForm = new FormGroup({
  password: new FormControl('', { nonNullable: true, validators: [Validators.required] }),
  confirmPassword: new FormControl('', { nonNullable: true, validators: [Validators.required] })
}, { validators: passwordsMatchValidator });   // el error vive en el GRUPO, no en un control

// El mensaje se muestra con this.registerForm.hasError('passwordsMismatch')
```

- Reactive Forms SIEMPRE: tipados, testeables, validación en TS. Importar `ReactiveFormsModule`.
- Template-driven (`FormsModule` + `[(ngModel)]`) está DEPRECADO — solo si un enunciado viejo lo
  exige textualmente. Su validación queda en atributos del HTML: no tipada y difícil de testear.
- `Validators.email` es laxo → para email serio usá `Validators.pattern` con regex que exija TLD.
- Validación entre dos campos → validador en el `FormGroup`, no en el `FormControl`.
- Los regex del FE y del BE deben ser IDÉNTICOS (contrato de validación coherente).
- `nonNullable: true` para evitar `string | null` en cada control.
- Mostrar errores solo si `touched` — no castigar al usuario antes de que interactúe.

---
