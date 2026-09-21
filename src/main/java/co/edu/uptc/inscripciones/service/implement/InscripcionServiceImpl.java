package co.edu.uptc.inscripciones.service.implement;
import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uptc.inscripciones.dto.request.InscripcionRequestDTO;
import co.edu.uptc.inscripciones.dto.response.InscripcionResponseDTO;
import co.edu.uptc.inscripciones.exception.RecursoNoEncontradoException;
import co.edu.uptc.inscripciones.mapper.InscripcionMapper;
import co.edu.uptc.inscripciones.model.EstadoInscripcion;
import co.edu.uptc.inscripciones.model.HistorialEstado;
import co.edu.uptc.inscripciones.model.Inscripcion;
import co.edu.uptc.inscripciones.model.Nota;
import co.edu.uptc.inscripciones.repository.InscripcionRepository;
import co.edu.uptc.inscripciones.service.HistorialEstadoService;
import co.edu.uptc.inscripciones.service.InscripcionService;
import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InscripcionServiceImpl implements InscripcionService {
    private final InscripcionRepository inscripcionRepository;
    private final HistorialEstadoService historialEstadoService;
    private final InscripcionMapper mapper; // 👈 inyéctalo aquí tambi
    @Override
    public Page<InscripcionResponseDTO> listar(Specification<Inscripcion> spec, Pageable pageable) {
        return inscripcionRepository.findAll(spec, pageable)
                .map(mapper::toResponseDTO); // 👈 mapea AQUÍ, con la sesión todavía abierta
    }   

    @Override
    public InscripcionResponseDTO obtenerPorId(Long id) {
        Inscripcion inscripcion = inscripcionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Inscripción no encontrada con id: " + id));
        return mapper.toResponseDTO(inscripcion);
    }
    @Override
    @Transactional // 👈 escritura, sobreescribe el readOnly de la clase
    public InscripcionResponseDTO crear(InscripcionRequestDTO dto) {
        Inscripcion inscripcion = mapper.toEntity(dto);
        inscripcion.setFechaInscripcion(LocalDate.now());
        if (inscripcion.getEstado() == null) inscripcion.setEstado(EstadoInscripcion.ACTIVA);
        return mapper.toResponseDTO(inscripcionRepository.save(inscripcion));
    }
    @Override
    @Transactional
    public InscripcionResponseDTO actualizar(Long id, InscripcionRequestDTO dto) {
        Inscripcion inscripcion = inscripcionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Inscripción no encontrada con id: " + id));
        return mapper.toResponseDTO(inscripcionRepository.save(inscripcion));
    }
    @Override
    @Transactional
    public InscripcionResponseDTO cambiarEstado(Long id, EstadoInscripcion nuevoEstado, String motivo) {
    Inscripcion inscripcion = inscripcionRepository.findById(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Inscripción no encontrada con id: " + id));

    EstadoInscripcion anterior = inscripcion.getEstado();
    inscripcion.setEstado(nuevoEstado);
    Inscripcion actualizada = inscripcionRepository.save(inscripcion);

    historialEstadoService.registrarCambio(actualizada, anterior, nuevoEstado, motivo);

    return mapper.toResponseDTO(actualizada);
}
    @Override
    public void eliminar(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'eliminar'");
    }


}
