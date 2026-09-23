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
import ar.edu.utn.frc.tup.p4.services.game.BoardFactory;
import ar.edu.utn.frc.tup.p4.services.game.state.GameState;
import ar.edu.utn.frc.tup.p4.services.game.state.GameStateRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static ar.edu.utn.frc.tup.p4.support.GameFixtures.GAME_ID;
import static ar.edu.utn.frc.tup.p4.support.GameFixtures.OWNER_ID;
import static ar.edu.utn.frc.tup.p4.support.GameFixtures.gameInProgress;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pure unit test: EVERY collaborator is mocked (repos, factory, registry, mapper).
 * The rules themselves are tested in their own classes; here we test orchestration,
 * ownership and error paths.
 */
@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("GameServiceImpl")
class GameServiceImplTest {

    private static final Long OTHER_USER_ID = 99L;

    @Mock
    private GameRepository gameRepository;

    @Mock
    private MoveRepository moveRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BoardFactory boardFactory;

    @Mock
    private GameStateRegistry gameStateRegistry;

    @Mock
    private GameMapper gameMapper;

    @Mock
    private GameState gameState;

    @InjectMocks
    private GameServiceImpl gameService;

    @Test
    @DisplayName("create builds a WAITING game with trimmed players, 3 lives each, and the board")
    void create_withValidRequest_buildsAndPersistsGame() {
        // GIVEN
        User owner = new User();
        owner.setId(OWNER_ID);
        GameCreateRequest request = new GameCreateRequest(List.of(" Ana ", "Beto"), 20);
        GameDTO dto = new GameDTO();
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.of(owner));
        when(gameRepository.save(any(Game.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(gameMapper.toDTO(any(Game.class))).thenReturn(dto);

        // WHEN
        GameDTO result = gameService.create(OWNER_ID, request);

        // THEN
        ArgumentCaptor<Game> captor = ArgumentCaptor.forClass(Game.class);
        verify(gameRepository).save(captor.capture());
        Game saved = captor.getValue();
        assertSame(dto, result);
        assertSame(owner, saved.getOwner());
        assertEquals(GameStatus.WAITING, saved.getStatus());
        assertEquals(20, saved.getBoardSize());
        assertEquals("Ana", saved.getPlayers().get(0).getName());
        assertEquals(1, saved.getPlayers().get(1).getTurnOrder());
        assertEquals(GameServiceImpl.INITIAL_LIVES, saved.getPlayers().get(1).getLives());
        verify(boardFactory).createBoard(saved);
    }

    @Test
    @DisplayName("create with a user that no longer exists throws Unauthorized and persists nothing")
    void create_whenUserMissing_throwsUnauthorized() {
        when(userRepository.findById(OWNER_ID)).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class,
                () -> gameService.create(OWNER_ID, new GameCreateRequest(List.of("Ana", "Beto"), 20)));
        verify(gameRepository, never()).save(any());
    }

    @Test
    @DisplayName("findMine maps every summary of the owner")
    void findMine_returnsMappedSummaries() {
        Game game = gameInProgress(10, "Ana", "Beto");
        GameSummaryDTO summary = new GameSummaryDTO();
        when(gameRepository.findSummariesByOwnerId(OWNER_ID)).thenReturn(List.of(game));
        when(gameMapper.toSummaryDTO(game)).thenReturn(summary);

        assertEquals(List.of(summary), gameService.findMine(OWNER_ID));
    }

    @Test
    @DisplayName("findById loads players AND cells (two queries) of an owned game")
    void findById_whenOwned_loadsBothCollections() {
        Game game = givenStoredGame();
        GameDTO dto = new GameDTO();
        when(gameMapper.toDTO(game)).thenReturn(dto);

        assertSame(dto, gameService.findById(GAME_ID, OWNER_ID));
        verify(gameRepository).findWithCellsById(GAME_ID);
    }

    @Test
    @DisplayName("findById of a missing game throws GameNotFound")
    void findById_whenMissing_throwsNotFound() {
        when(gameRepository.findWithPlayersById(GAME_ID)).thenReturn(Optional.empty());

        assertThrows(GameNotFoundException.class, () -> gameService.findById(GAME_ID, OWNER_ID));
    }

    @Test
    @DisplayName("findById of someone else's game answers NotFound too (does not leak the id)")
    void findById_whenNotOwner_throwsNotFound() {
        givenStoredGame();

        assertThrows(GameNotFoundException.class, () -> gameService.findById(GAME_ID, OTHER_USER_ID));
        verify(gameRepository, never()).findWithCellsById(GAME_ID);
    }

    @Test
    @DisplayName("start delegates to the state of the current status")
    void start_delegatesToCurrentState() {
        Game game = givenStoredGame();
        when(gameStateRegistry.get(GameStatus.IN_PROGRESS)).thenReturn(gameState);

        gameService.start(GAME_ID, OWNER_ID);

        verify(gameState).start(game);
        verify(gameMapper).toDTO(game);
    }

    @Test
    @DisplayName("roll delegates to the state, saves the move and returns move + game")
    void roll_withValidPlayer_savesMoveAndReturnsResult() {
        // GIVEN
        Game game = givenStoredGame();
        Player beto = game.getPlayers().get(1);
        Move move = new Move();
        MoveDTO moveDTO = new MoveDTO();
        GameDTO gameDTO = new GameDTO();
        when(gameStateRegistry.get(GameStatus.IN_PROGRESS)).thenReturn(gameState);
        when(gameState.roll(game, beto)).thenReturn(move);
        when(moveRepository.save(move)).thenReturn(move);
        when(gameMapper.toMoveDTO(move)).thenReturn(moveDTO);
        when(gameMapper.toDTO(game)).thenReturn(gameDTO);

        // WHEN
        RollResultDTO result = gameService.roll(GAME_ID, OWNER_ID, new RollRequest(beto.getId()));

        // THEN
        assertSame(moveDTO, result.getMove());
        assertSame(gameDTO, result.getGame());
    }

    @Test
    @DisplayName("roll with a player id from another game throws PlayerNotFound before touching the state")
    void roll_withUnknownPlayer_throwsPlayerNotFound() {
        givenStoredGame();

        assertThrows(PlayerNotFoundException.class,
                () -> gameService.roll(GAME_ID, OWNER_ID, new RollRequest(555L)));
        verify(gameStateRegistry, never()).get(any());
        verify(moveRepository, never()).save(any());
    }

    @Test
    @DisplayName("findMoves checks ownership first and maps the history")
    void findMoves_whenOwned_returnsMappedMoves() {
        givenStoredGame();
        Move move = new Move();
        MoveDTO dto = new MoveDTO();
        when(moveRepository.findByGameIdWithPlayer(GAME_ID)).thenReturn(List.of(move));
        when(gameMapper.toMoveDTO(move)).thenReturn(dto);

        List<MoveDTO> result = gameService.findMoves(GAME_ID, OWNER_ID);

        assertEquals(1, result.size());
        assertTrue(result.contains(dto));
    }

    @Test
    @DisplayName("findMoves of someone else's game throws NotFound and never reads the history")
    void findMoves_whenNotOwner_throwsNotFound() {
        givenStoredGame();

        assertThrows(GameNotFoundException.class, () -> gameService.findMoves(GAME_ID, OTHER_USER_ID));
        verify(moveRepository, never()).findByGameIdWithPlayer(any());
    }

    private Game givenStoredGame() {
        Game game = gameInProgress(10, "Ana", "Beto");
        when(gameRepository.findWithPlayersById(GAME_ID)).thenReturn(Optional.of(game));
        return game;
    }
}
