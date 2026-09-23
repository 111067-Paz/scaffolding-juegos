package ar.edu.utn.frc.tup.p4.services;

import ar.edu.utn.frc.tup.p4.entities.User;

/**
 * Token contract — consumers (AuthService, interceptor) depend on this
 * interface, never on the JWT implementation (DIP). Swapping JWT for
 * another token scheme touches only the impl.
 */
public interface TokenService {

    String generateToken(User user);

    /**
     * Validates the token signature and expiration.
     *
     * @return the authenticated user id (token subject)
     * @throws ar.edu.utn.frc.tup.p4.exceptions.UnauthorizedException if invalid/expired
     */
    Long validateAndGetUserId(String token);
}
