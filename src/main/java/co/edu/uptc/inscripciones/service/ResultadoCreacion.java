package co.edu.uptc.inscripciones.service;

import co.edu.uptc.inscripciones.dto.response.InscripcionResponseDTO;

/**
 * Resultado de crear una inscripción. repetida = true cuando la misma
 * Idempotency-Key ya había creado la inscripción: se devuelve la anterior
 * y no se crea otra.
 */
public record ResultadoCreacion(InscripcionResponseDTO inscripcion, boolean repetida) {
}
