package co.edu.uptc.inscripciones.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import co.edu.uptc.inscripciones.exception.ConflictoException;
import co.edu.uptc.inscripciones.exception.ReglaNegocioException;
import co.edu.uptc.inscripciones.exception.ServicioNoDisponibleException;
import lombok.extern.slf4j.Slf4j;

/**
 * El cupo vive en Materias (cursos.cupos_disponibles). Materias lo descuenta
 * con UNA sentencia condicional (UPDATE ... WHERE cupos_disponibles > 0),
 * así que dos inscripciones simultáneas nunca toman el mismo último cupo.
 *
 * reservar NO se reintenta: no es idempotente y un reintento podría
 * descontar dos cupos.
 */
@Component
@Slf4j
public class MateriasClient {

    private final RestClient client;

    public MateriasClient(@Qualifier("materiasRestClient") RestClient client) {
        this.client = client;
    }

    /**
     *   200 -> cupo reservado
     *   404 -> 422 CURSO_NO_EXISTE
     *   422 -> 422 CURSO_INACTIVO
     *   409 -> 409 SIN_CUPO
     *   sin respuesta -> 503
     */
    public void reservarCupo(Long cursoId) {
        try {
            client.post().uri("/internal/cursos/{id}/reservar", cursoId).retrieve().toBodilessEntity();
        } catch (RestClientResponseException e) {
            switch (e.getStatusCode().value()) {
                case 404 -> throw new ReglaNegocioException("CURSO_NO_EXISTE", "El curso " + cursoId + " no existe");
                case 422 -> throw new ReglaNegocioException("CURSO_INACTIVO", "El curso " + cursoId + " está INACTIVO");
                case 409 -> throw new ConflictoException("SIN_CUPO", "El curso " + cursoId + " no tiene cupos disponibles");
                default -> throw new ServicioNoDisponibleException("Materias", e);
            }
        } catch (ResourceAccessException e) {
            throw new ServicioNoDisponibleException("Materias", e);
        }
    }

    /**
     * Compensación: devuelve el cupo. Si falla solo se registra; Materias
     * nunca deja pasar los disponibles por encima del cupo total.
     */
    public void liberarCupo(Long cursoId) {
        try {
            client.post().uri("/internal/cursos/{id}/liberar", cursoId).retrieve().toBodilessEntity();
        } catch (RuntimeException e) {
            log.error("No se pudo liberar el cupo del curso {} (queda pendiente de conciliar): {}", cursoId, e.getMessage());
        }
    }
}
