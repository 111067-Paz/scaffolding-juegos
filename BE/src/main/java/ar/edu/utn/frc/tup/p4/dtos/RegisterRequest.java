package ar.edu.utn.frc.tup.p4.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Alta de usuario. La validación se dispara con @Valid en el controller.
 * El password viaja en claro SOLO en el request (HTTPS en producción);
 * el service lo hashea antes de persistir.
 *
 * Los regex son IDÉNTICOS a los del formulario reactivo del frontend
 * (register-page.ts): el contrato de validación tiene que coincidir en
 * ambas capas, el backend nunca confía solo en el cliente.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]*$",
            message = "Username must start with a letter and use only letters, numbers or underscore")
    private String username;

    @NotBlank(message = "Email is required")
    @Size(max = 120, message = "Email must not exceed 120 characters")
    // @Email por defecto acepta "asad@sa" (sin TLD); el regexp exige dominio.tld
    @Email(regexp = "^[\\w.%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$",
            message = "Email must be valid (e.g. name@domain.com)")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
            message = "Password must include an uppercase letter, a lowercase letter and a number")
    private String password;
}
