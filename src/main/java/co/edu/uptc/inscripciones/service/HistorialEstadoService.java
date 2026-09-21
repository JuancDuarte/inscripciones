package co.edu.uptc.inscripciones.service;

import java.util.List;

import co.edu.uptc.inscripciones.dto.response.HistorialEstadoResponseDTO;
import co.edu.uptc.inscripciones.model.EstadoInscripcion;
import co.edu.uptc.inscripciones.model.HistorialEstado;
import co.edu.uptc.inscripciones.model.Inscripcion;

public interface HistorialEstadoService {
    List<HistorialEstadoResponseDTO> listarPorInscripcion(Long inscripcionId);
    HistorialEstadoResponseDTO obtenerPorId(Long historialId);
    HistorialEstado registrarCambio(Inscripcion inscripcion, EstadoInscripcion anterior,
                                     EstadoInscripcion nuevo, String motivo); // uso interno, sigue en entidad
    void eliminar(Long historialId);
}