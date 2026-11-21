package co.edu.uptc.inscripciones.dto.response;

import java.time.LocalDateTime;

/** Estado actual del interruptor global de inscripción. */
public record ConfiguracionInscripcionResponseDTO(boolean habilitada, LocalDateTime actualizadoEn) {
}
