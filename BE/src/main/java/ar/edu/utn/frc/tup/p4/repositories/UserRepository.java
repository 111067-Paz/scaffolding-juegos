package ar.edu.utn.frc.tup.p4.repositories;

import ar.edu.utn.frc.tup.p4.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data deriva la query del nombre del método — sin SQL a mano.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
