package co.edu.uptc.inscripciones.dto.response;

import java.time.LocalDate;
import java.util.List;

import co.edu.uptc.inscripciones.model.EstadoInscripcion;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Builder 
public class InscripcionResponseDTO {
    private Long idInscripcion;
    private Long estudianteId;
    private Long cursoId;
    private String periodo;
    private EstadoInscripcion estado;
    private LocalDate fechaInscripcion;
    private List<NotaResponseDTO> notas;
    private List<HistorialEstadoResponseDTO> historial;

}
