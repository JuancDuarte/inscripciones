package co.edu.uptc.inscripciones.client;

import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import co.edu.uptc.inscripciones.exception.ReglaNegocioException;
import co.edu.uptc.inscripciones.exception.ServicioNoDisponibleException;
import lombok.extern.slf4j.Slf4j;

/**
 * Pregunta al módulo Estudiantes si el estudiante existe y está ACTIVO
 * (en Estudiantes, status = "active"). Es una lectura, así que se reintenta
 * una vez si el módulo no responde.
 *   404            -> 422 ESTUDIANTE_NO_EXISTE
 *   status!=active -> 422 ESTUDIANTE_INACTIVO
 *   sin respuesta  -> 503
 */
@Component
@Slf4j
public class EstudiantesClient {

    private final RestClient client;

    public EstudiantesClient(@Qualifier("estudiantesRestClient") RestClient client) {
        this.client = client;
    }

    public void validarActivo(Long estudianteId) {
        Map<?, ?> estudiante = obtener(estudianteId, 2);
        Object status = estudiante == null ? null : estudiante.get("status");
        if (!"active".equals(status)) {
            throw new ReglaNegocioException("ESTUDIANTE_INACTIVO",
                    "El estudiante " + estudianteId + " no está ACTIVO (estado: " + status + ")");
        }
    }

    private Map<?, ?> obtener(Long id, int intentos) {
        try {
            return client.get().uri("/api/estudiantes/{id}", id).retrieve().body(Map.class);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
                throw new ReglaNegocioException("ESTUDIANTE_NO_EXISTE", "El estudiante " + id + " no existe");
            }
            if (e.getStatusCode().is5xxServerError() && intentos > 1) {
                return obtener(id, intentos - 1);
            }
            throw new ServicioNoDisponibleException("Estudiantes", e);
        } catch (ResourceAccessException e) {
            if (intentos > 1) {
                log.warn("Estudiantes no respondió ({}), reintentando una vez", e.getMessage());
                return obtener(id, intentos - 1);
            }
            throw new ServicioNoDisponibleException("Estudiantes", e);
        }
    }
}
