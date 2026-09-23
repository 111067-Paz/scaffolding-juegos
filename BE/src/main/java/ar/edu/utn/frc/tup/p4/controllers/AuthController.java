package ar.edu.utn.frc.tup.p4.controllers;

import ar.edu.utn.frc.tup.p4.configs.AuthCookieFactory;
import ar.edu.utn.frc.tup.p4.configs.JwtAuthInterceptor;
import ar.edu.utn.frc.tup.p4.dtos.AuthResponse;
import ar.edu.utn.frc.tup.p4.dtos.LoginRequest;
import ar.edu.utn.frc.tup.p4.dtos.RegisterRequest;
import ar.edu.utn.frc.tup.p4.dtos.UserDTO;
import ar.edu.utn.frc.tup.p4.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * Auth with basic JWT (no Spring Security).
 * register/login set the HttpOnly cookie (used by the SPA) AND return the token in
 * the body (used by API clients with "Authorization: Bearer"). The SPA never stores it.
 * Thin controller: validates the body (@Valid) and delegates everything to the service.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Register, login, logout and current session")
public class AuthController {

    private final AuthService authService;
    private final AuthCookieFactory authCookieFactory;

    @PostMapping("/register")
    @Operation(summary = "Register a new user and open a session")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse created = authService.register(request);
        return ResponseEntity
                .created(URI.create("/api/auth/users/" + created.getUser().getId()))
                .header(HttpHeaders.SET_COOKIE, authCookieFactory.create(created.getToken()).toString())
                .body(created);
    }

    @PostMapping("/login")
    @Operation(summary = "Log in with username and password")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, authCookieFactory.create(response.getToken()).toString())
                .body(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "Close the session (deletes the auth cookie)")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, authCookieFactory.clear().toString())
                .build();
    }

    @GetMapping("/me")
    @Operation(summary = "Current session user (401 when there is no valid session)")
    public ResponseEntity<UserDTO> me(@RequestAttribute(JwtAuthInterceptor.USER_ID_ATTRIBUTE) Long userId) {
        return ResponseEntity.ok(authService.getCurrentUser(userId));
    }
}
