package ar.edu.utn.frc.tup.p4.external;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * The external API is NEVER called for real: MockRestServiceServer intercepts
 * the RestClient and answers with canned JSON (happy path + outage).
 */
@Tag("unit")
@DisplayName("ExternalApiClient")
class ExternalApiClientTest {

    private MockRestServiceServer server;
    private ExternalApiClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://api.test");
        server = MockRestServiceServer.bindTo(builder).build();
        client = new ExternalApiClient(builder.build());
    }

    @Test
    @DisplayName("getUsers maps the JSON array")
    void getUsers_whenApiResponds_returnsUsers() {
        server.expect(requestTo("https://api.test/users"))
                .andRespond(withSuccess("[{\"id\":1,\"name\":\"Ana\",\"email\":\"ana@test.com\"}]",
                        MediaType.APPLICATION_JSON));

        List<ExternalUserResponse> users = client.getUsers();

        assertEquals(1, users.size());
        assertEquals("Ana", users.get(0).getName());
    }

    @Test
    @DisplayName("getUserById maps one user")
    void getUserById_whenApiResponds_returnsUser() {
        server.expect(requestTo("https://api.test/users/1"))
                .andRespond(withSuccess("{\"id\":1,\"name\":\"Ana\",\"email\":\"ana@test.com\"}",
                        MediaType.APPLICATION_JSON));

        assertEquals("ana@test.com", client.getUserById(1L).getEmail());
    }

    @Test
    @DisplayName("an API outage becomes a controlled IllegalStateException")
    void getUsers_whenApiFails_throwsIllegalState() {
        server.expect(requestTo("https://api.test/users")).andRespond(withServerError());
        assertThrows(IllegalStateException.class, () -> client.getUsers());

        server.reset();
        server.expect(requestTo("https://api.test/users/2")).andRespond(withServerError());
        assertThrows(IllegalStateException.class, () -> client.getUserById(2L));
    }
}
