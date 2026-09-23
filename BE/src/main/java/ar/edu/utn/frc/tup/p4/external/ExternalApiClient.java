package ar.edu.utn.frc.tup.p4.external;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

/**
 * ONLY this module talks to the external API.
 * In automated tests this client is ALWAYS mocked — never call the real API.
 *
 * @Lazy: no se instancia al arrancar el contexto (arrastraría el RestClient/HttpClient);
 * se crea recién cuando un colaborador lo necesita.
 */
@Component
@Lazy
@RequiredArgsConstructor
@Slf4j
public class ExternalApiClient {

    private final RestClient externalRestClient;

    public List<ExternalUserResponse> getUsers() {
        try {
            return externalRestClient.get()
                    .uri("/users")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<ExternalUserResponse>>() { });
        } catch (RestClientException exception) {
            log.error("External API call failed: {}", exception.getMessage());
            throw new IllegalStateException("External service is unavailable. Please try again later.");
        }
    }

    public ExternalUserResponse getUserById(Long id) {
        try {
            return externalRestClient.get()
                    .uri("/users/{id}", id)
                    .retrieve()
                    .body(ExternalUserResponse.class);
        } catch (RestClientException exception) {
            log.error("External API call failed for user {}: {}", id, exception.getMessage());
            throw new IllegalStateException("External service is unavailable. Please try again later.");
        }
    }
}
