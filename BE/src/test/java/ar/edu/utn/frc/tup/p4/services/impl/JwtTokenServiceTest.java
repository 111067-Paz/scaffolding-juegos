package ar.edu.utn.frc.tup.p4.services.impl;

import ar.edu.utn.frc.tup.p4.entities.User;
import ar.edu.utn.frc.tup.p4.exceptions.UnauthorizedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Pure unit test — no Spring context: the service takes its config by
 * constructor, so tests build it directly with test values.
 */
@Tag("unit")
@DisplayName("JwtTokenService")
class JwtTokenServiceTest {

    private static final String SECRET = "test-secret-key-of-at-least-32-bytes!!";
    private static final String OTHER_SECRET = "another-secret-key-of-32-bytes-min!!!";
    private static final Long USER_ID = 7L;

    private final JwtTokenService tokenService = new JwtTokenService(SECRET, 60);

    @Test
    @DisplayName("generate then validate returns the user id (round trip)")
    void validateAndGetUserId_whenTokenIsValid_returnsUserId() {
        // GIVEN
        String token = tokenService.generateToken(sampleUser());
        assertNotNull(token);

        // WHEN
        Long userId = tokenService.validateAndGetUserId(token);

        // THEN
        assertEquals(USER_ID, userId);
    }

    @Test
    @DisplayName("a token signed with a DIFFERENT secret is rejected")
    void validateAndGetUserId_whenSignatureDoesNotMatch_throwsUnauthorized() {
        // GIVEN — token issued by a service with another secret
        JwtTokenService intruderService = new JwtTokenService(OTHER_SECRET, 60);
        String foreignToken = intruderService.generateToken(sampleUser());

        // WHEN + THEN
        UnauthorizedException exception = assertThrows(UnauthorizedException.class,
                () -> tokenService.validateAndGetUserId(foreignToken));
        assertEquals("Invalid or expired token", exception.getMessage());
    }

    @Test
    @DisplayName("an expired token is rejected")
    void validateAndGetUserId_whenTokenExpired_throwsUnauthorized() {
        // GIVEN — negative expiration: the token is born already expired (limit)
        JwtTokenService expiredIssuer = new JwtTokenService(SECRET, -1);
        String expiredToken = expiredIssuer.generateToken(sampleUser());

        // WHEN + THEN
        assertThrows(UnauthorizedException.class,
                () -> tokenService.validateAndGetUserId(expiredToken));
    }

    @Test
    @DisplayName("garbage input (not a JWT) is rejected")
    void validateAndGetUserId_whenTokenIsGarbage_throwsUnauthorized() {
        assertThrows(UnauthorizedException.class,
                () -> tokenService.validateAndGetUserId("not-a-jwt-at-all"));
    }

    private User sampleUser() {
        User user = new User();
        user.setId(USER_ID);
        user.setUsername("lpaz");
        user.setEmail("lpaz@utn.edu.ar");
        user.setPassword("$2a$10$hashedhashedhashedhashed");
        return user;
    }
}
