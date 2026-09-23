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
import ar.edu.utn.frc.tup.p4.services.AuthService;
import ar.edu.utn.frc.tup.p4.services.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Auth logic: BCrypt hashing + basic JWT (no Spring Security).
 * Constructor injection (private final + @RequiredArgsConstructor);
 * PasswordEncoder and TokenService are injected so tests can mock them.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Uniqueness rules: username and email cannot repeat
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("username", request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("email", request.getEmail());
        }

        // The password is hashed BEFORE touching the database — never stored raw
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        User saved = userRepository.save(userMapper.toEntity(request, hashedPassword));
        log.info("Registered user {} with id {}", saved.getUsername(), saved.getId());
        return buildAuthResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        // Same generic message on both error paths: we never reveal whether
        // the user exists. matches() compares the raw password to the hash.
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        log.info("User {} logged in", user.getUsername());
        return buildAuthResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getCurrentUser(Long userId) {
        // A valid token whose user no longer exists is treated as "no session".
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("Session user no longer exists"));
        return userMapper.toDTO(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        return AuthResponse.builder()
                .token(tokenService.generateToken(user))
                .user(userMapper.toDTO(user))
                .build();
    }
}
