package ar.edu.utn.frc.tup.p4;

import ar.edu.utn.frc.tup.p4.services.game.DiceRoller;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The whole reference game over HTTP with the real context and H2:
 * create → start → roll until someone wins → history → FINISHED rejects rolls.
 * The die is a @MockitoBean returning 3, so the game is deterministic.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Tag("integration")
@DisplayName("Game full flow")
class GameFlowIntegrationTest {

    private final MockMvc mockMvc;

    @MockitoBean
    private DiceRoller diceRoller;

    GameFlowIntegrationTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    @DisplayName("a 10-cell game with a die of 3 is won by the first player on turn 5")
    void fullGame_withFixedDice_firstPlayerWins() throws Exception {
        // GIVEN
        when(diceRoller.roll()).thenReturn(3);
        String token = registerAndGetToken("player1", "p1@utn.edu.ar");
        String gameJson = mockMvc.perform(post("/api/games")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"player_names": ["Ana", "Beto"], "board_size": 10}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.cells.length()").value(10))
                .andExpect(jsonPath("$.created_at").exists())
                .andReturn().getResponse().getContentAsString();
        Integer gameId = JsonPath.read(gameJson, "$.id");
        Integer anaId = JsonPath.read(gameJson, "$.players[0].id");
        Integer betoId = JsonPath.read(gameJson, "$.players[1].id");

        // WHEN — start and alternate turns.
        // Board 10: 4=ADVANCE, 5=BACK, 6=LOSE_TURN, 7=TRAP, 8=ADVANCE, 9=GOAL.
        mockMvc.perform(post("/api/games/{id}/start", gameId).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.current_player_id").value(anaId));

        roll(token, gameId, anaId).andExpect(jsonPath("$.move.to_position").value(3));
        roll(token, gameId, betoId).andExpect(jsonPath("$.move.to_position").value(3));
        // Ana 3 → 6 (LOSE_TURN): her next turn is skipped
        roll(token, gameId, anaId).andExpect(jsonPath("$.move.cell_type").value("LOSE_TURN"));
        // Beto 3 → 6 (LOSE_TURN) too; rotation consumes Ana's flag, then Beto's → Ana plays
        roll(token, gameId, betoId).andExpect(jsonPath("$.game.current_player_id").value(anaId));
        // Ana 6 → 9 = GOAL
        roll(token, gameId, anaId)
                .andExpect(jsonPath("$.game.status").value("FINISHED"))
                .andExpect(jsonPath("$.game.winner_id").value(anaId));

        // THEN — history and terminal state
        mockMvc.perform(get("/api/games/{id}/moves", gameId).header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[0].player_name").value("Ana"));
        roll(token, gameId, betoId).andExpect(status().isConflict());
        mockMvc.perform(get("/api/games").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(jsonPath("$[0].winner_name").value("Ana"));
    }

    @Test
    @DisplayName("rolling out of turn returns 409 and another user's game returns 404")
    void roll_outOfTurnOrForeignGame_returnsErrors() throws Exception {
        // GIVEN
        when(diceRoller.roll()).thenReturn(1);
        String ownerToken = registerAndGetToken("owner1", "owner1@utn.edu.ar");
        String strangerToken = registerAndGetToken("stranger", "stranger@utn.edu.ar");
        String gameJson = mockMvc.perform(post("/api/games")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"player_names": ["Ana", "Beto"], "board_size": 20}
                                """))
                .andReturn().getResponse().getContentAsString();
        Integer gameId = JsonPath.read(gameJson, "$.id");
        Integer betoId = JsonPath.read(gameJson, "$.players[1].id");

        // WHEN + THEN
        roll(ownerToken, gameId, betoId).andExpect(status().isConflict());   // WAITING
        mockMvc.perform(post("/api/games/{id}/start", gameId).header(HttpHeaders.AUTHORIZATION, "Bearer " + ownerToken));
        roll(ownerToken, gameId, betoId)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("It is not Beto's turn, it is Ana's turn"));
        mockMvc.perform(get("/api/games/{id}", gameId).header(HttpHeaders.AUTHORIZATION, "Bearer " + strangerToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("creating a game with one player returns 400 (Bean Validation)")
    void create_withOnePlayer_returns400() throws Exception {
        String token = registerAndGetToken("lonely", "lonely@utn.edu.ar");
        mockMvc.perform(post("/api/games")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"player_names": ["Ana"], "board_size": 20}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    private org.springframework.test.web.servlet.ResultActions roll(String token, Integer gameId, Integer playerId)
            throws Exception {
        return mockMvc.perform(post("/api/games/{id}/roll", gameId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"player_id\": " + playerId + "}"));
    }

    private String registerAndGetToken(String username, String email) throws Exception {
        String body = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "%s", "email": "%s", "password": "Supersecret1"}
                                """.formatted(username, email)))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.token");
    }
}
