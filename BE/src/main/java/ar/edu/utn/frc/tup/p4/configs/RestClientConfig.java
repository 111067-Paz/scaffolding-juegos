package ar.edu.utn.frc.tup.p4.configs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.client.RestClient;

/**
 * HTTP client bean for consuming an external API.
 * Base URL is externalized — change it in application.properties.
 *
 * @Lazy: el RestClient (y su HttpClient subyacente) se crea SOLO cuando se usa de verdad,
 * no al arrancar el contexto. Así los @SpringBootTest no dependen de la red para levantar.
 */
@Configuration
public class RestClientConfig {

    @Bean
    @Lazy
    public RestClient externalRestClient(
            @Value("${external.api.base-url:https://jsonplaceholder.typicode.com}") String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
}
