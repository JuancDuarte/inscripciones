package co.edu.uptc.inscripciones.service.implement;

import java.time.LocalDate;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import co.edu.uptc.inscripciones.dto.response.HistorialEstadoResponseDTO;
import co.edu.uptc.inscripciones.exception.RecursoNoEncontradoException;
import co.edu.uptc.inscripciones.mapper.InscripcionMapper;
import co.edu.uptc.inscripciones.model.EstadoInscripcion;
import co.edu.uptc.inscripciones.model.HistorialEstado;
import co.edu.uptc.inscripciones.model.Inscripcion;
import co.edu.uptc.inscripciones.repository.HistorialEstadoRepository;
import co.edu.uptc.inscripciones.repository.InscripcionRepository;
import co.edu.uptc.inscripciones.service.HistorialEstadoService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HistorialEstadoServiceImpl implements HistorialEstadoService {

    private final HistorialEstadoRepository historialEstadoRepository;
    private final InscripcionRepository inscripcionRepository;
     private final InscripcionMapper mapper;

     @Override
    public List<HistorialEstadoResponseDTO> listarPorInscripcion(Long inscripcionId) {
        if (!inscripcionRepository.existsById(inscripcionId)) {
            throw new RecursoNoEncontradoException("Inscripción no encontrada con id: " + inscripcionId);
        }
        return historialEstadoRepository.findByInscripcion_IdInscripcion(inscripcionId).stream()
                .map(mapper::toHistorialResponseDTO)
                .toList();
    }

    @Override
    public HistorialEstadoResponseDTO obtenerPorId(Long historialId) {
        HistorialEstado historial = historialEstadoRepository.findById(historialId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Historial no encontrado con id: " + historialId));
        return mapper.toHistorialResponseDTO(historial);
    }

    @Override
    public HistorialEstado registrarCambio(Inscripcion inscripcion, EstadoInscripcion anterior,
                                            EstadoInscripcion nuevo, String motivo) {
        // Recibe la entidad managed directamente desde InscripcionServiceImpl — no pasa por DTO aquí,
        // porque este método nunca lo llama el Controller, solo otro Service en la misma transacción.
        HistorialEstado historial = HistorialEstado.builder()
                .estadoAnterior(anterior)
                .estadoNuevo(nuevo)
                .fechaCambio(LocalDate.now())
                .motivo(motivo)
                .inscripcion(inscripcion)
                .build();
        return historialEstadoRepository.save(historial);
    }

    @Override
    public void eliminar(Long historialId) {
        if (!historialEstadoRepository.existsById(historialId)) {
            throw new RecursoNoEncontradoException("Historial no encontrado con id: " + historialId);
        }
        historialEstadoRepository.deleteById(historialId);
    }
}