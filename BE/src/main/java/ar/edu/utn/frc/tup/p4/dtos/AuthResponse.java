package ar.edu.utn.frc.tup.p4.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Auth response: the JWT plus the public user data (nested DTO).
 * The token travels ONLY here — never persisted, never logged.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String token;

    private UserDTO user;
}
