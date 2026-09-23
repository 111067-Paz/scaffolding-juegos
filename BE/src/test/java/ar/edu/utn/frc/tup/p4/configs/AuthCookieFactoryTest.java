package ar.edu.utn.frc.tup.p4.configs;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("unit")
@DisplayName("AuthCookieFactory")
class AuthCookieFactoryTest {

    @Test
    @DisplayName("create builds an HttpOnly, SameSite=Strict cookie scoped to /api that lives as long as the JWT")
    void create_buildsSecureCookie() {
        ResponseCookie cookie = new AuthCookieFactory(60, true).create("jwt");

        assertEquals(AuthCookieFactory.COOKIE_NAME, cookie.getName());
        assertEquals("jwt", cookie.getValue());
        assertTrue(cookie.isHttpOnly());
        assertTrue(cookie.isSecure());
        assertEquals("Strict", cookie.getSameSite());
        assertEquals("/api", cookie.getPath());
        assertEquals(Duration.ofMinutes(60), cookie.getMaxAge());
    }

    @Test
    @DisplayName("clear builds an empty expired cookie (the browser deletes it)")
    void clear_buildsExpiredCookie() {
        ResponseCookie cookie = new AuthCookieFactory(60, false).clear();

        assertEquals("", cookie.getValue());
        assertEquals(Duration.ZERO, cookie.getMaxAge());
    }
}
