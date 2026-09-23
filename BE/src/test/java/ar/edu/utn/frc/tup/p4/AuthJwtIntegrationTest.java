package ar.edu.utn.frc.tup.p4;

import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full auth flow against the real context: register → token (body + HttpOnly cookie)
 * → protected endpoint with Bearer, with cookie, and without anything.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Tag("integration")
@DisplayName("Auth JWT full flow")
class AuthJwtIntegrationTest {

    private final MockMvc mockMvc;

    AuthJwtIntegrationTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    @DisplayName("register returns a token that grants access with the Bearer header")
    void register_thenAccessProtectedEndpointWithBearer_returns200() throws Exception {
        // GIVEN + WHEN
        MvcResult result = register("jwtuser", "jwt@utn.edu.ar");
        String token = JsonPath.read(result.getResponse().getContentAsString(), "$.token");

        // THEN
        mockMvc.perform(get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("jwtuser"));
    }

    @Test
    @DisplayName("register sets the HttpOnly cookie that grants access (SPA path)")
    void register_thenAccessProtectedEndpointWithCookie_returns200() throws Exception {
        // GIVEN + WHEN
        MvcResult result = register("cookieuser", "cookie@utn.edu.ar");
        Cookie cookie = result.getResponse().getCookie("AUTH_TOKEN");

        // THEN
        mockMvc.perform(get("/api/games").cookie(cookie))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("a protected endpoint without token returns 401 with the ErrorApi contract")
    void getGames_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/games"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("a protected endpoint with a fake token returns 401")
    void getGames_withInvalidToken_returns401() throws Exception {
        mockMvc.perform(get("/api/games")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer fake.tampered.token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    private MvcResult register(String username, String email) throws Exception {
        return mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username": "%s", "email": "%s", "password": "Supersecret1"}
                                """.formatted(username, email)))
                .andExpect(status().isCreated())
                .andReturn();
    }
}
