package ar.edu.utn.frc.tup.p4.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * SOLO hashing de contraseñas. Proviene de spring-security-crypto, que es una
 * librería de utilidades criptográficas: NO levanta el filter chain de Spring
 * Security, NO hay login form, NO hay JWT. Es únicamente el algoritmo BCrypt.
 *
 * Se expone como bean para inyectarlo por constructor (sin @Autowired) y poder
 * mockearlo en los tests del service.
 */
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
