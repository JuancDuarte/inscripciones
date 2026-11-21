package co.edu.uptc.inscripciones.dto.request;

import jakarta.validation.constraints.NotNull;

/** Nuevo estado del interruptor global de inscripción. */
public record CambiarConfiguracionInscripcionRequestDTO(
        @NotNull(message = "El campo habilitada es obligatorio") Boolean habilitada) {
}
