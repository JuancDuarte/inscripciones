package co.edu.uptc.inscripciones.service.implement;

import java.util.List;

import org.springframework.stereotype.Service;

import co.edu.uptc.inscripciones.dto.request.NotaRequestDTO;
import co.edu.uptc.inscripciones.dto.response.NotaResponseDTO;
import co.edu.uptc.inscripciones.exception.RecursoNoEncontradoException;
import co.edu.uptc.inscripciones.mapper.InscripcionMapper;
import co.edu.uptc.inscripciones.model.Inscripcion;
import co.edu.uptc.inscripciones.model.Nota;
import co.edu.uptc.inscripciones.repository.InscripcionRepository;
import co.edu.uptc.inscripciones.repository.NotaRepository;
import co.edu.uptc.inscripciones.service.InscripcionService;
import co.edu.uptc.inscripciones.service.NotaService;
import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class NotaServiceImpl implements NotaService {
    private final NotaRepository notaRepository;
    private final InscripcionRepository inscripcionRepository;
    private final InscripcionService inscripcionService; // solo para validar que la inscripción exista
    private final InscripcionMapper mapper; // para convertir entre Nota y NotaResponseDTO

    @Override
public List<NotaResponseDTO> listarPorInscripcion(Long inscripcionId) {
    if (!inscripcionRepository.existsById(inscripcionId)) {
        throw new RecursoNoEncontradoException("Inscripción no encontrada con id: " + inscripcionId);
    }
    return notaRepository.findByInscripcion_IdInscripcion(inscripcionId).stream()
            .map(mapper::toNotaResponseDTO)
            .toList();
}

@Override
public NotaResponseDTO obtenerPorId(Long notaId) {
    Nota nota = notaRepository.findById(notaId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Nota no encontrada con id: " + notaId));
    return mapper.toNotaResponseDTO(nota);
}
@Override
    public NotaResponseDTO crear(Long inscripcionId, NotaRequestDTO dto) {
        Inscripcion inscripcion = inscripcionRepository.findById(inscripcionId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Inscripción no encontrada con id: " + inscripcionId));

        Nota nota = mapper.toEntity(dto);
        nota.setInscripcion(inscripcion); // ahora sí es una entidad real, managed por esta transacción
        return mapper.toNotaResponseDTO(notaRepository.save(nota));
    }

@Override
public NotaResponseDTO actualizar(Long notaId, NotaRequestDTO dto) {
    Nota existente = notaRepository.findById(notaId)
            .orElseThrow(() -> new RecursoNoEncontradoException("Nota no encontrada con id: " + notaId));
    existente.setTipo(dto.getTipo());
    existente.setValor(dto.getValor());
    existente.setPorcentaje(dto.getPorcentaje());
    return mapper.toNotaResponseDTO(notaRepository.save(existente));
}

@Override
public void eliminar(Long notaId) {
    if (!notaRepository.existsById(notaId)) {
        throw new RecursoNoEncontradoException("Nota no encontrada con id: " + notaId);
    }
    notaRepository.deleteById(notaId);
}


}
