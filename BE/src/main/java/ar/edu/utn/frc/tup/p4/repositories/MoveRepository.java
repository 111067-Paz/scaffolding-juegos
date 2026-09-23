package ar.edu.utn.frc.tup.p4.repositories;

import ar.edu.utn.frc.tup.p4.entities.Move;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/** Move history; the player is fetched in the same query (the DTO shows its name). */
public interface MoveRepository extends JpaRepository<Move, Long> {

    @Query("select m from Move m join fetch m.player where m.game.id = :gameId order by m.id asc")
    List<Move> findByGameIdWithPlayer(@Param("gameId") Long gameId);
}
