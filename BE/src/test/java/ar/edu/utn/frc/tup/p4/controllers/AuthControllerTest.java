package ar.edu.utn.frc.tup.p4.controllers;

import ar.edu.utn.frc.tup.p4.configs.AuthCookieFactory;
import ar.edu.utn.frc.tup.p4.configs.JwtAuthInterceptor;
import ar.edu.utn.frc.tup.p4.dtos.AuthResponse;
import ar.edu.utn.frc.tup.p4.dtos.UserDTO;
import ar.edu.utn.frc.tup.p4.exceptions.GlobalExceptionHandler;
import ar.edu.utn.frc.tup.p4.exceptions.InvalidCredentialsException;
import ar.edu.utn.frc.tup.p4.exceptions.UnauthorizedException;
import ar.edu.utn.frc.tup.p4.exceptions.UserAlreadyExistsException;
import ar.edu.utn.frc.tup.p4.services.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web slice con standalone MockMvc — sin contexto de Spring, rápido.
 * El GlobalExceptionHandler se registra para que los caminos de error
 * devuelvan los status reales (409, 401, 400).
 */
@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("AuthController")
class AuthControllerTest {

    private static final Long USER_ID = 1L;

    @Mock
    private AuthService authService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        // Real cookie factory (pure value object builder) + mocked service
        AuthController authController = new AuthController(authService, new AuthCookieFactory(60, false));
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/auth/register devuelve 201 con Location, token y sin exponer password")
    void register_whenRequestIsValid_returns201WithLocationAndToken() throws Exception {
        // GIVEN
        when(authService.register(any())).thenReturn(sampleAuthResponse());

        // WHEN + THEN
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "lpaz", "email": "lpaz@utn.edu.ar", "password": "Supersecret1"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/auth/users/1"))
                .andExpect(jsonPath("$.token").value("jwt.token.value"))
                .andExpect(jsonPath("$.user.id").value(1))
                .andExpect(jsonPath("$.user.username").value("lpaz"))
                .andExpect(jsonPath("$.user.password").doesNotExist())
                .andExpect(header().string("Set-Cookie", containsString("AUTH_TOKEN=jwt.token.value")))
                .andExpect(header().string("Set-Cookie", containsString("HttpOnly")))
                .andExpect(header().string("Set-Cookie", containsString("SameSite=Strict")));
    }

    @Test
    @DisplayName("POST /api/auth/register devuelve 409 cuando el usuario ya existe")
    void register_whenUserExists_returns409() throws Exception {
        // GIVEN
        when(authService.register(any()))
                .thenThrow(new UserAlreadyExistsException("username", "lpaz"));

        // WHEN + THEN
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "lpaz", "email": "lpaz@utn.edu.ar", "password": "Supersecret1"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("POST /api/auth/register devuelve 400 cuando la validación falla")
    void register_whenPasswordTooShort_returns400() throws Exception {
        // GIVEN — password de 3 chars (< 8), email inválido (límites)

        // WHEN + THEN — Bean Validation corre antes de llegar al service
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "ab", "email": "not-an-email", "password": "123"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /api/auth/login devuelve 200 con token y usuario")
    void login_whenCredentialsValid_returns200WithToken() throws Exception {
        // GIVEN
        when(authService.login(any())).thenReturn(sampleAuthResponse());

        // WHEN + THEN
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "lpaz", "password": "Supersecret1"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt.token.value"))
                .andExpect(jsonPath("$.user.username").value("lpaz"))
                .andExpect(header().string("Set-Cookie", containsString("AUTH_TOKEN=jwt.token.value")));
    }

    @Test
    @DisplayName("POST /api/auth/logout devuelve 204 y una cookie vencida (Max-Age=0)")
    void logout_always_returns204AndExpiredCookie() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isNoContent())
                .andExpect(header().string("Set-Cookie", containsString("AUTH_TOKEN=")))
                .andExpect(header().string("Set-Cookie", containsString("Max-Age=0")));
    }

    @Test
    @DisplayName("GET /api/auth/me devuelve el usuario de la sesión")
    void me_whenSessionValid_returns200WithUser() throws Exception {
        // GIVEN — the interceptor already put the user id in the request
        when(authService.getCurrentUser(USER_ID)).thenReturn(sampleAuthResponse().getUser());

        // WHEN + THEN
        mockMvc.perform(get("/api/auth/me").requestAttr(JwtAuthInterceptor.USER_ID_ATTRIBUTE, USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("lpaz"));
    }

    @Test
    @DisplayName("GET /api/auth/me devuelve 401 si el usuario ya no existe")
    void me_whenUserMissing_returns401() throws Exception {
        when(authService.getCurrentUser(USER_ID)).thenThrow(new UnauthorizedException("Session user no longer exists"));

        mockMvc.perform(get("/api/auth/me").requestAttr(JwtAuthInterceptor.USER_ID_ATTRIBUTE, USER_ID))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("POST /api/auth/login devuelve 401 con credenciales inválidas")
    void login_whenCredentialsInvalid_returns401() throws Exception {
        // GIVEN — error path SIEMPRE testeado
        when(authService.login(any())).thenThrow(new InvalidCredentialsException());

        // WHEN + THEN
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "lpaz", "password": "wrong"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    private AuthResponse sampleAuthResponse() {
        UserDTO user = UserDTO.builder()
                .id(USER_ID)
                .username("lpaz")
                .email("lpaz@utn.edu.ar")
                .build();
        return AuthResponse.builder()
                .token("jwt.token.value")
                .user(user)
                .build();
    }
}
