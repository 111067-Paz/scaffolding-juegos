package ar.edu.utn.frc.tup.p4.configs;

import ar.edu.utn.frc.tup.p4.exceptions.UnauthorizedException;
import ar.edu.utn.frc.tup.p4.services.TokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.WebUtils;

/**
 * Guards protected paths (registered in WebConfig) WITHOUT Spring Security.
 * Accepts the token from two sources, in this order:
 * 1. "Authorization: Bearer &lt;jwt&gt;" — API clients (Postman, the grader's tests).
 * 2. The HttpOnly AUTH_TOKEN cookie — the Angular SPA (never touches the token).
 * The authenticated user id is exposed as a request attribute: ownership checks
 * use it, NEVER an id coming from the request body.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthInterceptor implements HandlerInterceptor {

    public static final String USER_ID_ATTRIBUTE = "authenticatedUserId";
    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenService tokenService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = resolveToken(request);
        if (token == null) {
            throw new UnauthorizedException("Missing authentication token");
        }
        Long userId = tokenService.validateAndGetUserId(token);
        request.setAttribute(USER_ID_ATTRIBUTE, userId);
        return true;
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        Cookie cookie = WebUtils.getCookie(request, AuthCookieFactory.COOKIE_NAME);
        if (cookie != null && !cookie.getValue().isBlank()) {
            return cookie.getValue();
        }
        return null;
    }
}
