package ar.edu.utn.frc.tup.p4.controllers;

import ar.edu.utn.frc.tup.p4.configs.JwtAuthInterceptor;
import ar.edu.utn.frc.tup.p4.dtos.GameDTO;
import ar.edu.utn.frc.tup.p4.dtos.GameSummaryDTO;
import ar.edu.utn.frc.tup.p4.dtos.MoveDTO;
import ar.edu.utn.frc.tup.p4.dtos.RollRequest;
import ar.edu.utn.frc.tup.p4.dtos.RollResultDTO;
import ar.edu.utn.frc.tup.p4.entities.GameStatus;
import ar.edu.utn.frc.tup.p4.exceptions.GameNotFoundException;
import ar.edu.utn.frc.tup.p4.exceptions.GlobalExceptionHandler;
import ar.edu.utn.frc.tup.p4.exceptions.InvalidMoveException;
import ar.edu.utn.frc.tup.p4.services.GameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web slice with standalone MockMvc + the real GlobalExceptionHandler.
 * Checks status codes, the snake_case JSON contract and the per-field date format.
 */
@ExtendWith(MockitoExtension.class)
@Tag("unit")
@DisplayName("GameController")
class GameControllerTest {

    private static final Long USER_ID = 1L;
    private static final Long GAME_ID = 10L;

    @Mock
    private GameService gameService;

    @InjectMocks
    private GameController gameController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(gameController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/games returns 201 with Location and snake_case JSON")
    void create_whenValid_returns201() throws Exception {
        when(gameService.create(eq(USER_ID), any())).thenReturn(sampleGame());

        mockMvc.perform(post("/api/games")
                        .requestAttr(JwtAuthInterceptor.USER_ID_ATTRIBUTE, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"player_names": ["Ana", "Beto"], "board_size": 20}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/games/10"))
                .andExpect(jsonPath("$.board_size").value(20))
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.created_at").value("17-09-2026 21:30:00"));
    }

    @Test
    @DisplayName("POST /api/games returns 400 when a player name breaks the regex (limit)")
    void create_whenPlayerNameInvalid_returns400() throws Exception {
        mockMvc.perform(post("/api/games")
                        .requestAttr(JwtAuthInterceptor.USER_ID_ATTRIBUTE, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"player_names": ["A", "Beto<script>"], "board_size": 9}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        verify(gameService, never()).create(any(), any());
    }

    @Test
    @DisplayName("POST /api/games returns 400 with a malformed JSON body")
    void create_whenBodyMalformed_returns400() throws Exception {
        mockMvc.perform(post("/api/games")
                        .requestAttr(JwtAuthInterceptor.USER_ID_ATTRIBUTE, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed request body"));
    }

    @Test
    @DisplayName("GET /api/games returns 200 with my games")
    void findMine_returns200() throws Exception {
        GameSummaryDTO summary = GameSummaryDTO.builder().id(GAME_ID).winnerName("Ana").build();
        when(gameService.findMine(USER_ID)).thenReturn(List.of(summary));

        mockMvc.perform(get("/api/games").requestAttr(JwtAuthInterceptor.USER_ID_ATTRIBUTE, USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].winner_name").value("Ana"));
    }

    @Test
    @DisplayName("GET /api/games/{id} returns 200, or 404 when it is not mine")
    void findById_foundAndNotFound() throws Exception {
        when(gameService.findById(GAME_ID, USER_ID)).thenReturn(sampleGame());
        when(gameService.findById(99L, USER_ID)).thenThrow(new GameNotFoundException(99L));

        mockMvc.perform(get("/api/games/{id}", GAME_ID).requestAttr(JwtAuthInterceptor.USER_ID_ATTRIBUTE, USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
        mockMvc.perform(get("/api/games/{id}", 99L).requestAttr(JwtAuthInterceptor.USER_ID_ATTRIBUTE, USER_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Game 99 not found"));
    }

    @Test
    @DisplayName("POST /api/games/{id}/start returns 200")
    void start_returns200() throws Exception {
        when(gameService.start(GAME_ID, USER_ID)).thenReturn(sampleGame());

        mockMvc.perform(post("/api/games/{id}/start", GAME_ID)
                        .requestAttr(JwtAuthInterceptor.USER_ID_ATTRIBUTE, USER_ID))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/games/{id}/roll returns 200 with move + game")
    void roll_whenValid_returns200() throws Exception {
        MoveDTO move = MoveDTO.builder().diceValue(4).toPosition(4).build();
        when(gameService.roll(GAME_ID, USER_ID, new RollRequest(100L)))
                .thenReturn(new RollResultDTO(move, sampleGame()));

        mockMvc.perform(post("/api/games/{id}/roll", GAME_ID)
                        .requestAttr(JwtAuthInterceptor.USER_ID_ATTRIBUTE, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"player_id\": 100}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.move.dice_value").value(4))
                .andExpect(jsonPath("$.game.id").value(10));
    }

    @Test
    @DisplayName("POST /api/games/{id}/roll returns 409 out of turn, 400 without player_id")
    void roll_errorPaths() throws Exception {
        when(gameService.roll(GAME_ID, USER_ID, new RollRequest(101L)))
                .thenThrow(new InvalidMoveException("It is not Beto's turn, it is Ana's turn"));

        mockMvc.perform(post("/api/games/{id}/roll", GAME_ID)
                        .requestAttr(JwtAuthInterceptor.USER_ID_ATTRIBUTE, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"player_id\": 101}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
        mockMvc.perform(post("/api/games/{id}/roll", GAME_ID)
                        .requestAttr(JwtAuthInterceptor.USER_ID_ATTRIBUTE, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/games/{id}/moves returns 200 with the history")
    void findMoves_returns200() throws Exception {
        when(gameService.findMoves(GAME_ID, USER_ID))
                .thenReturn(List.of(MoveDTO.builder().playerName("Ana").build()));

        mockMvc.perform(get("/api/games/{id}/moves", GAME_ID)
                        .requestAttr(JwtAuthInterceptor.USER_ID_ATTRIBUTE, USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].player_name").value("Ana"));
    }

    private GameDTO sampleGame() {
        return GameDTO.builder()
                .id(GAME_ID)
                .status(GameStatus.WAITING)
                .boardSize(20)
                .players(List.of())
                .cells(List.of())
                .createdAt(LocalDateTime.of(2026, 9, 17, 21, 30, 0))
                .build();
    }
}
