package ar.edu.utn.frc.tup.p4.services.impl;

import ar.edu.utn.frc.tup.p4.dtos.GameCreateRequest;
import ar.edu.utn.frc.tup.p4.dtos.GameDTO;
import ar.edu.utn.frc.tup.p4.dtos.GameSummaryDTO;
import ar.edu.utn.frc.tup.p4.dtos.MoveDTO;
import ar.edu.utn.frc.tup.p4.dtos.RollRequest;
import ar.edu.utn.frc.tup.p4.dtos.RollResultDTO;
import ar.edu.utn.frc.tup.p4.entities.Game;
import ar.edu.utn.frc.tup.p4.entities.GameStatus;
import ar.edu.utn.frc.tup.p4.entities.Move;
import ar.edu.utn.frc.tup.p4.entities.Player;
import ar.edu.utn.frc.tup.p4.entities.User;
import ar.edu.utn.frc.tup.p4.exceptions.GameNotFoundException;
import ar.edu.utn.frc.tup.p4.exceptions.PlayerNotFoundException;
import ar.edu.utn.frc.tup.p4.exceptions.UnauthorizedException;
import ar.edu.utn.frc.tup.p4.mappers.GameMapper;
import ar.edu.utn.frc.tup.p4.repositories.GameRepository;
import ar.edu.utn.frc.tup.p4.repositories.MoveRepository;
import ar.edu.utn.frc.tup.p4.repositories.UserRepository;
import ar.edu.utn.frc.tup.p4.services.GameService;
import ar.edu.utn.frc.tup.p4.services.game.BoardFactory;
import ar.edu.utn.frc.tup.p4.services.game.state.GameStateRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Orchestrates the game use cases: loads the entities (JOIN FETCH), checks
 * ownership, delegates the rules to the State/Chain/Strategy beans and persists.
 * The game logic lives in services/game — this class has no switch and no rules.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GameServiceImpl implements GameService {

    static final int INITIAL_LIVES = 3;

    private final GameRepository gameRepository;
    private final MoveRepository moveRepository;
    private final UserRepository userRepository;
    private final BoardFactory boardFactory;
    private final GameStateRegistry gameStateRegistry;
    private final GameMapper gameMapper;

    @Override
    @Transactional
    public GameDTO create(Long userId, GameCreateRequest request) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("Session user no longer exists"));

        Game game = new Game();
        game.setOwner(owner);
        game.setStatus(GameStatus.WAITING);
        game.setBoardSize(request.getBoardSize());
        game.setCurrentTurn(0);

        List<String> names = request.getPlayerNames();
        for (int order = 0; order < names.size(); order++) {
            Player player = new Player();
            player.setName(names.get(order).trim());
            player.setTurnOrder(order);
            player.setPosition(0);
            player.setLives(INITIAL_LIVES);
            game.addPlayer(player);
        }
        boardFactory.createBoard(game);

        Game saved = gameRepository.save(game);
        log.info("User {} created game {} with {} players", userId, saved.getId(), names.size());
        return gameMapper.toDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameSummaryDTO> findMine(Long userId) {
        return gameRepository.findSummariesByOwnerId(userId).stream()
                .map(gameMapper::toSummaryDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public GameDTO findById(Long gameId, Long userId) {
        return gameMapper.toDTO(loadOwnedGame(gameId, userId));
    }

    @Override
    @Transactional
    public GameDTO start(Long gameId, Long userId) {
        Game game = loadOwnedGame(gameId, userId);
        gameStateRegistry.get(game.getStatus()).start(game);
        log.info("Game {} started", gameId);
        return gameMapper.toDTO(game);
    }

    @Override
    @Transactional
    public RollResultDTO roll(Long gameId, Long userId, RollRequest request) {
        Game game = loadOwnedGame(gameId, userId);
        Player player = game.getPlayers().stream()
                .filter(candidate -> candidate.getId().equals(request.getPlayerId()))
                .findFirst()
                .orElseThrow(() -> new PlayerNotFoundException(request.getPlayerId(), gameId));

        Move move = gameStateRegistry.get(game.getStatus()).roll(game, player);
        Move savedMove = moveRepository.save(move);

        return RollResultDTO.builder()
                .move(gameMapper.toMoveDTO(savedMove))
                .game(gameMapper.toDTO(game))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MoveDTO> findMoves(Long gameId, Long userId) {
        loadOwnedGame(gameId, userId);
        return moveRepository.findByGameIdWithPlayer(gameId).stream()
                .map(gameMapper::toMoveDTO)
                .toList();
    }

    /**
     * Loads players (query 1) and cells (query 2) of the SAME managed instance
     * and checks ownership. Someone else's game answers 404, like a missing one.
     */
    private Game loadOwnedGame(Long gameId, Long userId) {
        Game game = gameRepository.findWithPlayersById(gameId)
                .orElseThrow(() -> new GameNotFoundException(gameId));
        if (!game.getOwner().getId().equals(userId)) {
            throw new GameNotFoundException(gameId);
        }
        gameRepository.findWithCellsById(gameId);
        return game;
    }
}
