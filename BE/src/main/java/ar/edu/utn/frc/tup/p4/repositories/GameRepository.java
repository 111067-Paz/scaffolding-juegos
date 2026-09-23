package ar.edu.utn.frc.tup.p4.repositories;

import ar.edu.utn.frc.tup.p4.entities.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * N+1 is solved with @Query + JOIN FETCH (project standard, no @EntityGraph, never EAGER).
 * Players and cells are two List collections: fetching both in ONE query throws
 * MultipleBagFetchException, so the service runs two queries in the same transaction
 * (the second one fills the cells of the already-managed Game instance).
 */
public interface GameRepository extends JpaRepository<Game, Long> {

    @Query("select distinct g from Game g left join fetch g.players where g.id = :id")
    Optional<Game> findWithPlayersById(@Param("id") Long id);

    @Query("select distinct g from Game g left join fetch g.cells where g.id = :id")
    Optional<Game> findWithCellsById(@Param("id") Long id);

    @Query("select g from Game g left join fetch g.winner where g.owner.id = :ownerId order by g.createdAt desc")
    List<Game> findSummariesByOwnerId(@Param("ownerId") Long ownerId);
}
