package ar.edu.utn.frc.tup.p4.configs;

import ar.edu.utn.frc.tup.p4.exceptions.UnauthorizedException;
import ar.edu.utn.frc.tup.p4.services.TokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import jakarta.servlet.http.Cookie;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pure unit test: mock TokenService, mock servlet request/response.
 * Every branch of preHandle gets its own test (limits of the header check).
 */
@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("JwtAuthInterceptor")
class JwtAuthInterceptorTest {

    private static final String TOKEN = "valid.jwt.token";
    private static final Long USER_ID = 7L;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private JwtAuthInterceptor interceptor;

    private final MockHttpServletRequest request = new MockHttpServletRequest();
    private final MockHttpServletResponse response = new MockHttpServletResponse();

    @Test
    @DisplayName("with a valid Bearer token lets the request pass and exposes the user id")
    void preHandle_whenBearerTokenValid_returnsTrueAndSetsUserId() {
        // GIVEN
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN);
        when(tokenService.validateAndGetUserId(TOKEN)).thenReturn(USER_ID);

        // WHEN
        boolean allowed = interceptor.preHandle(request, response, new Object());

        // THEN
        assertTrue(allowed);
        assertEquals(USER_ID, request.getAttribute(JwtAuthInterceptor.USER_ID_ATTRIBUTE));
    }

    @Test
    @DisplayName("without Authorization header throws Unauthorized and never touches the TokenService")
    void preHandle_whenHeaderMissing_throwsUnauthorized() {
        // GIVEN — no header at all

        // WHEN + THEN
        UnauthorizedException exception = assertThrows(UnauthorizedException.class,
                () -> interceptor.preHandle(request, response, new Object()));
        assertTrue(exception.getMessage().contains("token"));
        verify(tokenService, never()).validateAndGetUserId(anyString());
    }

    @Test
    @DisplayName("with a non-Bearer scheme throws Unauthorized (limit: prefix check)")
    void preHandle_whenSchemeIsNotBearer_throwsUnauthorized() {
        // GIVEN
        request.addHeader(HttpHeaders.AUTHORIZATION, "Basic dXNlcjpwYXNz");

        // WHEN + THEN
        assertThrows(UnauthorizedException.class,
                () -> interceptor.preHandle(request, response, new Object()));
        verify(tokenService, never()).validateAndGetUserId(anyString());
    }

    @Test
    @DisplayName("with an invalid token propagates the Unauthorized from the TokenService")
    void preHandle_whenTokenInvalid_propagatesUnauthorized() {
        // GIVEN
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN);
        when(tokenService.validateAndGetUserId(TOKEN))
                .thenThrow(new UnauthorizedException("Invalid or expired token"));

        // WHEN + THEN
        assertThrows(UnauthorizedException.class,
                () -> interceptor.preHandle(request, response, new Object()));
    }

    @Test
    @DisplayName("without header but with the AUTH_TOKEN cookie lets the request pass (SPA path)")
    void preHandle_whenCookiePresent_returnsTrueAndSetsUserId() {
        // GIVEN
        request.setCookies(new Cookie(AuthCookieFactory.COOKIE_NAME, TOKEN));
        when(tokenService.validateAndGetUserId(TOKEN)).thenReturn(USER_ID);

        // WHEN
        boolean allowed = interceptor.preHandle(request, response, new Object());

        // THEN
        assertTrue(allowed);
        assertEquals(USER_ID, request.getAttribute(JwtAuthInterceptor.USER_ID_ATTRIBUTE));
    }

    @Test
    @DisplayName("with an empty AUTH_TOKEN cookie throws Unauthorized (limit: blank value)")
    void preHandle_whenCookieBlank_throwsUnauthorized() {
        // GIVEN — the cookie a logout leaves behind
        request.setCookies(new Cookie(AuthCookieFactory.COOKIE_NAME, ""));

        // WHEN + THEN
        assertThrows(UnauthorizedException.class,
                () -> interceptor.preHandle(request, response, new Object()));
        verify(tokenService, never()).validateAndGetUserId(anyString());
    }

    @Test
    @DisplayName("the Bearer header wins over the cookie when both are present")
    void preHandle_whenHeaderAndCookie_usesHeader() {
        // GIVEN
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + TOKEN);
        request.setCookies(new Cookie(AuthCookieFactory.COOKIE_NAME, "cookie.token"));
        when(tokenService.validateAndGetUserId(TOKEN)).thenReturn(USER_ID);

        // WHEN
        interceptor.preHandle(request, response, new Object());

        // THEN
        verify(tokenService, never()).validateAndGetUserId("cookie.token");
    }
}
