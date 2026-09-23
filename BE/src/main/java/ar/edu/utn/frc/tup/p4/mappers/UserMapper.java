package ar.edu.utn.frc.tup.p4.mappers;

import ar.edu.utn.frc.tup.p4.dtos.RegisterRequest;
import ar.edu.utn.frc.tup.p4.dtos.UserDTO;
import ar.edu.utn.frc.tup.p4.entities.User;
import org.springframework.stereotype.Component;

/**
 * Mapper como @Component → se inyecta y se mockea en los tests.
 * El service NUNCA hace 'new UserMapper()'.
 */
@Component
public class UserMapper {

    public UserDTO toDTO(User entity) {
        return UserDTO.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .email(entity.getEmail())
                .build();
    }

    /**
     * Construye la entidad a partir del request + el password YA hasheado.
     * El hash lo calcula el service (tiene el PasswordEncoder), no el mapper.
     */
    public User toEntity(RegisterRequest request, String hashedPassword) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(hashedPassword);
        return user;
    }
}
