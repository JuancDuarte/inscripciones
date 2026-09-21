package co.edu.uptc.inscripciones.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import co.edu.uptc.inscripciones.dto.request.InscripcionRequestDTO;
import co.edu.uptc.inscripciones.dto.response.InscripcionResponseDTO;
import co.edu.uptc.inscripciones.model.EstadoInscripcion;
import co.edu.uptc.inscripciones.model.Inscripcion;
import co.edu.uptc.inscripciones.model.Nota;

public interface InscripcionService {
    Page<InscripcionResponseDTO> listar(Specification<Inscripcion> spec, Pageable pageable);
    InscripcionResponseDTO obtenerPorId(Long id);
    InscripcionResponseDTO crear(InscripcionRequestDTO dto);
    InscripcionResponseDTO actualizar(Long id, InscripcionRequestDTO dto);
    InscripcionResponseDTO cambiarEstado(Long id, EstadoInscripcion nuevoEstado, String motivo);
    void eliminar(Long id);
}