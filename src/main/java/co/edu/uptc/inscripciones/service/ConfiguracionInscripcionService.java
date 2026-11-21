package co.edu.uptc.inscripciones.service;

import co.edu.uptc.inscripciones.dto.response.ConfiguracionInscripcionResponseDTO;

public interface ConfiguracionInscripcionService {

    /** Estado actual; si nunca se ha guardado, la inscripción está habilitada. */
    ConfiguracionInscripcionResponseDTO obtener();

    /** Cambia el estado del interruptor global. */
    ConfiguracionInscripcionResponseDTO cambiar(boolean habilitada);
}
