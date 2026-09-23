package ar.edu.utn.frc.tup.p4.services.impl;

import ar.edu.utn.frc.tup.p4.dtos.AuthResponse;
import ar.edu.utn.frc.tup.p4.dtos.LoginRequest;
import ar.edu.utn.frc.tup.p4.dtos.RegisterRequest;
import ar.edu.utn.frc.tup.p4.dtos.UserDTO;
import ar.edu.utn.frc.tup.p4.entities.User;
import ar.edu.utn.frc.tup.p4.exceptions.InvalidCredentialsException;
import ar.edu.utn.frc.tup.p4.exceptions.UnauthorizedException;
import ar.edu.utn.frc.tup.p4.exceptions.UserAlreadyExistsException;
import ar.edu.utn.frc.tup.p4.mappers.UserMapper;
import ar.edu.utn.frc.tup.p4.repositories.UserRepository;
import ar.edu.utn.frc.tup.p4.services.TokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pure unit test: MockitoExtension, no Spring context.
 * ALL dependencies mocked (repo, mapper, encoder, token service).
 * Naming: methodName_scenario_expected. Structure: GIVEN / WHEN / THEN.
 */
@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("AuthServiceImpl")
class AuthServiceImplTest {

    private static final Long USER_ID = 1L;
    private static final String USERNAME = "lpaz";
    private static final String EMAIL = "lpaz@utn.edu.ar";
    private static final String RAW_PASSWORD = "supersecret1";
    private static final String HASHED_PASSWORD = "$2a$10$hashedhashedhashedhashed";
    private static final String TOKEN = "generated.jwt.token";

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    @DisplayName("register: con datos únicos hashea el password, persiste y devuelve token + usuario")
    void register_whenUsernameAndEmailAreFree_hashesPasswordAndReturnsTokenWithUser() {
        // GIVEN
        RegisterRequest request = sampleRegisterRequest();
        User entity = sampleUser();
        when(userRepository.existsByUsername(USERNAME)).thenReturn(false);
        when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(RAW_PASSWORD)).thenReturn(HASHED_PASSWORD);
        when(userMapper.toEntity(request, HASHED_PASSWORD)).thenReturn(entity);
        when(userRepository.save(entity)).thenReturn(entity);
        when(userMapper.toDTO(entity)).thenReturn(sampleDTO());
        when(tokenService.generateToken(entity)).thenReturn(TOKEN);

        // WHEN
        AuthResponse result = authService.register(request);

        // THEN
        assertEquals(TOKEN, result.getToken());
        assertEquals(USER_ID, result.getUser().getId());
        assertEquals(USERNAME, result.getUser().getUsername());
        verify(passwordEncoder).encode(RAW_PASSWORD);   // password NUNCA se guarda en claro
        verify(userRepository).save(entity);
    }

    @Test
    @DisplayName("register: si el username ya existe lanza UserAlreadyExists y no persiste")
    void register_whenUsernameTaken_throwsAndDoesNotSave() {
        // GIVEN
        RegisterRequest request = sampleRegisterRequest();
        when(userRepository.existsByUsername(USERNAME)).thenReturn(true);

        // WHEN + THEN
        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class,
                () -> authService.register(request));
        assertTrue(exception.getMessage().contains("username"));
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(anyString());
        verify(tokenService, never()).generateToken(any());
    }

    @Test
    @DisplayName("register: si el email ya existe lanza UserAlreadyExists y no persiste")
    void register_whenEmailTaken_throwsAndDoesNotSave() {
        // GIVEN
        RegisterRequest request = sampleRegisterRequest();
        when(userRepository.existsByUsername(USERNAME)).thenReturn(false);
        when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

        // WHEN + THEN
        UserAlreadyExistsException exception = assertThrows(UserAlreadyExistsException.class,
                () -> authService.register(request));
        assertTrue(exception.getMessage().contains("email"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("login: con credenciales correctas devuelve token + usuario")
    void login_whenCredentialsAreValid_returnsTokenWithUser() {
        // GIVEN
        LoginRequest request = sampleLoginRequest();
        User entity = sampleUser();
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(entity));
        when(passwordEncoder.matches(RAW_PASSWORD, HASHED_PASSWORD)).thenReturn(true);
        when(userMapper.toDTO(entity)).thenReturn(sampleDTO());
        when(tokenService.generateToken(entity)).thenReturn(TOKEN);

        // WHEN
        AuthResponse result = authService.login(request);

        // THEN
        assertEquals(TOKEN, result.getToken());
        assertEquals(USERNAME, result.getUser().getUsername());
    }

    @Test
    @DisplayName("login: si el usuario no existe lanza InvalidCredentials (mensaje genérico)")
    void login_whenUserNotFound_throwsInvalidCredentials() {
        // GIVEN
        LoginRequest request = sampleLoginRequest();
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        // WHEN + THEN
        InvalidCredentialsException exception = assertThrows(InvalidCredentialsException.class,
                () -> authService.login(request));
        assertEquals("Invalid username or password", exception.getMessage());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("login: si el password no coincide lanza InvalidCredentials y no emite token")
    void login_whenPasswordDoesNotMatch_throwsInvalidCredentials() {
        // GIVEN
        LoginRequest request = sampleLoginRequest();
        User entity = sampleUser();
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(entity));
        when(passwordEncoder.matches(RAW_PASSWORD, HASHED_PASSWORD)).thenReturn(false);

        // WHEN + THEN
        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
        verify(userMapper, never()).toDTO(any());
        verify(tokenService, never()).generateToken(any());
    }

    @Test
    @DisplayName("getCurrentUser: con un id existente devuelve el DTO del usuario")
    void getCurrentUser_whenUserExists_returnsDTO() {
        // GIVEN
        User entity = sampleUser();
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(entity));
        when(userMapper.toDTO(entity)).thenReturn(sampleDTO());

        // WHEN
        UserDTO result = authService.getCurrentUser(USER_ID);

        // THEN
        assertEquals(USERNAME, result.getUsername());
    }

    @Test
    @DisplayName("getCurrentUser: si el usuario del token ya no existe lanza Unauthorized")
    void getCurrentUser_whenUserMissing_throwsUnauthorized() {
        // GIVEN
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        // WHEN + THEN
        assertThrows(UnauthorizedException.class, () -> authService.getCurrentUser(USER_ID));
    }

    private RegisterRequest sampleRegisterRequest() {
        return RegisterRequest.builder()
                .username(USERNAME)
                .email(EMAIL)
                .password(RAW_PASSWORD)
                .build();
    }

    private LoginRequest sampleLoginRequest() {
        return LoginRequest.builder()
                .username(USERNAME)
                .password(RAW_PASSWORD)
                .build();
    }

    private User sampleUser() {
        User user = new User();
        user.setId(USER_ID);
        user.setUsername(USERNAME);
        user.setEmail(EMAIL);
        user.setPassword(HASHED_PASSWORD);
        return user;
    }

    private UserDTO sampleDTO() {
        return UserDTO.builder()
                .id(USER_ID)
                .username(USERNAME)
                .email(EMAIL)
                .build();
    }
}
