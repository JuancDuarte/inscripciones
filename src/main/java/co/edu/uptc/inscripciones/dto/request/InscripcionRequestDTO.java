package co.edu.uptc.inscripciones.dto.request;

import co.edu.uptc.inscripciones.model.EstadoInscripcion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter 
public class InscripcionRequestDTO {
    @NotNull(message = "El id del estudiante es obligatorio")
    private Long estudianteId;

    @NotNull(message = "El id del curso es obligatorio")
    private Long cursoId;

    @NotBlank(message = "El periodo es obligatorio")
    private String periodo;

    private EstadoInscripcion estado; // opcional al crear, default ACTIVA en el service

}
