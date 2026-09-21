package co.edu.uptc.inscripciones.dto.request;

import co.edu.uptc.inscripciones.model.EstadoInscripcion;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter 
public class CambiarEstadoRequestDTO {
    @NotNull(message = "El nuevo estado es obligatorio")
    private EstadoInscripcion nuevoEstado;

    private String motivo;

}
