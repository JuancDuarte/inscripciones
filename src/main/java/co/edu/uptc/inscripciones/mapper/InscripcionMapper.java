package co.edu.uptc.inscripciones.mapper;

import org.springframework.stereotype.Component;

import co.edu.uptc.inscripciones.dto.request.InscripcionRequestDTO;
import co.edu.uptc.inscripciones.dto.request.NotaRequestDTO;
import co.edu.uptc.inscripciones.dto.response.HistorialEstadoResponseDTO;
import co.edu.uptc.inscripciones.dto.response.InscripcionResponseDTO;
import co.edu.uptc.inscripciones.dto.response.NotaResponseDTO;
import co.edu.uptc.inscripciones.model.EstadoInscripcion;
import co.edu.uptc.inscripciones.model.HistorialEstado;
import co.edu.uptc.inscripciones.model.Inscripcion;
import co.edu.uptc.inscripciones.model.Nota;

@Component 
public class InscripcionMapper {
    public Inscripcion toEntity(InscripcionRequestDTO dto) {
        return Inscripcion.builder()
                .estudianteId(dto.getEstudianteId())
                .cursoId(dto.getCursoId())
                .periodo(dto.getPeriodo())
                .estado(dto.getEstado() != null ? dto.getEstado() : EstadoInscripcion.ACTIVA)
                .build();
    }

    public Nota toEntity(NotaRequestDTO dto) {
        return Nota.builder()
                .tipo(dto.getTipo())
                .valor(dto.getValor())
                .porcentaje(dto.getPorcentaje())
                .build();
    }

    public InscripcionResponseDTO toResponseDTO(Inscripcion entity) {
        return InscripcionResponseDTO.builder()
                .idInscripcion(entity.getIdInscripcion())
                .estudianteId(entity.getEstudianteId())
                .cursoId(entity.getCursoId())
                .periodo(entity.getPeriodo())
                .estado(entity.getEstado())
                .fechaInscripcion(entity.getFechaInscripcion())
                .notas(entity.getNotas().stream().map(this::toNotaResponseDTO).toList())
                .historial(entity.getHistorial().stream().map(this::toHistorialResponseDTO).toList())
                .build();
    }

    public NotaResponseDTO toNotaResponseDTO(Nota nota) {
        return NotaResponseDTO.builder()
                .idNota(nota.getIdNota())
                .tipo(nota.getTipo())
                .valor(nota.getValor())
                .porcentaje(nota.getPorcentaje())
                .build();
    }

    public HistorialEstadoResponseDTO toHistorialResponseDTO(HistorialEstado h) {
        return HistorialEstadoResponseDTO.builder()
                .idHistorial(h.getIdHistorial())
                .estadoAnterior(h.getEstadoAnterior())
                .estadoNuevo(h.getEstadoNuevo())
                .fechaCambio(h.getFechaCambio())
                .motivo(h.getMotivo())
                .build();
    }

}
