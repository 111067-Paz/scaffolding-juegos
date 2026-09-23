package ar.edu.utn.frc.tup.p4.services;

import ar.edu.utn.frc.tup.p4.dtos.GameCreateRequest;
import ar.edu.utn.frc.tup.p4.dtos.GameDTO;
import ar.edu.utn.frc.tup.p4.dtos.GameSummaryDTO;
import ar.edu.utn.frc.tup.p4.dtos.MoveDTO;
import ar.edu.utn.frc.tup.p4.dtos.RollRequest;
import ar.edu.utn.frc.tup.p4.dtos.RollResultDTO;

import java.util.List;

/**
 * Game use cases. Every method receives the authenticated user id (from the token)
 * and validates ownership — a user can only see and play their own games.
 */
public interface GameService {

    GameDTO create(Long userId, GameCreateRequest request);

    List<GameSummaryDTO> findMine(Long userId);

    GameDTO findById(Long gameId, Long userId);

    GameDTO start(Long gameId, Long userId);

    RollResultDTO roll(Long gameId, Long userId, RollRequest request);

    List<MoveDTO> findMoves(Long gameId, Long userId);
}
