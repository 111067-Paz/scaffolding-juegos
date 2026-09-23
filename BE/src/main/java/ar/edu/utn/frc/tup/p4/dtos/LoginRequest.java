package ar.edu.utn.frc.tup.p4.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Credenciales de login. Solo validamos que no vengan vacías:
 * el detalle de "usuario o contraseña incorrectos" lo decide el service
 * (sin revelar CUÁL de los dos falló — buena práctica de seguridad).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;
}
