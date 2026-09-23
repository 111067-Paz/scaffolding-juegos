package ar.edu.utn.frc.tup.p4.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Builds the auth cookie that carries the JWT to the browser.
 * HttpOnly: JavaScript can NOT read it (an XSS cannot steal the token).
 * SameSite=Strict + Path=/api: only sent to our own API, never cross-site.
 * The SPA and the API share the origin (dev proxy / Nginx), so no CORS is involved.
 */
@Component
public class AuthCookieFactory {

    public static final String COOKIE_NAME = "AUTH_TOKEN";
    private static final String COOKIE_PATH = "/api";
    private static final String SAME_SITE = "Strict";

    private final Duration maxAge;
    private final boolean secure;

    public AuthCookieFactory(
            @Value("${app.jwt.expiration-minutes}") long expirationMinutes,
            @Value("${app.auth.cookie-secure}") boolean secure) {
        this.maxAge = Duration.ofMinutes(expirationMinutes);
        this.secure = secure;
    }

    /** Cookie that stores the token for as long as the JWT is valid. */
    public ResponseCookie create(String token) {
        return build(token, maxAge);
    }

    /** Expired empty cookie: the browser deletes it (logout). */
    public ResponseCookie clear() {
        return build("", Duration.ZERO);
    }

    private ResponseCookie build(String value, Duration cookieMaxAge) {
        return ResponseCookie.from(COOKIE_NAME, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite(SAME_SITE)
                .path(COOKIE_PATH)
                .maxAge(cookieMaxAge)
                .build();
    }
}
