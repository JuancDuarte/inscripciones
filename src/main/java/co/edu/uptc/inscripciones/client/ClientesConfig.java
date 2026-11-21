package co.edu.uptc.inscripciones.client;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Clientes HTTP hacia los otros módulos. Timeout corto (2 s por defecto):
 * si un módulo no responde, Inscripciones contesta 503 rápido en vez de
 * dejar la petición colgada.
 */
@Configuration
public class ClientesConfig {

    @Bean
    RestClient estudiantesRestClient(@Value("${clientes.estudiantes.url}") String url,
                                     @Value("${clientes.timeout:2s}") Duration timeout) {
        return RestClient.builder().baseUrl(url).requestFactory(factory(timeout)).build();
    }

    @Bean
    RestClient materiasRestClient(@Value("${clientes.materias.url}") String url,
                                  @Value("${clientes.timeout:2s}") Duration timeout) {
        return RestClient.builder().baseUrl(url).requestFactory(factory(timeout)).build();
    }

    private static SimpleClientHttpRequestFactory factory(Duration timeout) {
        SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(timeout);
        f.setReadTimeout(timeout);
        return f;
    }
}
