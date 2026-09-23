package ar.edu.utn.frc.tup.p4.services;

import ar.edu.utn.frc.tup.p4.dtos.AuthResponse;
import ar.edu.utn.frc.tup.p4.dtos.LoginRequest;
import ar.edu.utn.frc.tup.p4.dtos.RegisterRequest;
import ar.edu.utn.frc.tup.p4.dtos.UserDTO;

/**
 * Auth contract — consumers (controllers, tests) depend on this interface,
 * never on the implementation (DIP).
 */
public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    /** Current session user; the SPA calls it to restore the session after a reload. */
    UserDTO getCurrentUser(Long userId);
}
