package co.edu.uptc.inscripciones.service;

import java.util.List;

import co.edu.uptc.inscripciones.dto.request.NotaRequestDTO;
import co.edu.uptc.inscripciones.dto.response.NotaResponseDTO;
import co.edu.uptc.inscripciones.model.Nota;

public interface NotaService {
    List<NotaResponseDTO> listarPorInscripcion(Long inscripcionId);
    NotaResponseDTO obtenerPorId(Long notaId);
    NotaResponseDTO crear(Long inscripcionId, NotaRequestDTO dto);
    NotaResponseDTO actualizar(Long notaId, NotaRequestDTO dto);
    void eliminar(Long notaId);
}

