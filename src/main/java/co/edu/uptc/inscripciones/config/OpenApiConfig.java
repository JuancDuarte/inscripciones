package co.edu.uptc.inscripciones.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration 
public class OpenApiConfig {
     @Bean
    public OpenAPI inscripcionesOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Inscripciones Service API")
                        .description("Módulo de Inscripciones — Laboratorio 3, Sistemas Distribuidos UPTC")
                        .version("v1.0"));
    }

}
