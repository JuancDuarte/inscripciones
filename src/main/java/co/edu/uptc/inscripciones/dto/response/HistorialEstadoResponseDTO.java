package co.edu.uptc.inscripciones.dto.response;

import java.time.LocalDate;

import co.edu.uptc.inscripciones.model.EstadoInscripcion;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

// HistorialEstadoResponseDTO.java
@Getter @Setter @Builder
public class HistorialEstadoResponseDTO {
    private Long idHistorial;
    private EstadoInscripcion estadoAnterior;
    private EstadoInscripcion estadoNuevo;
    private LocalDate fechaCambio;
    private String motivo;
}