package ar.edu.utn.frc.tup.p4.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Respuesta de auth. EXPONE solo datos públicos del usuario.
 * NUNCA incluye el password (ni el hash) — regla de oro.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {

    private Long id;

    private String username;

    private String email;
}
