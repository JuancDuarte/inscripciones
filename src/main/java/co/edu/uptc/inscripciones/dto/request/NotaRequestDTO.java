package co.edu.uptc.inscripciones.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter 
public class NotaRequestDTO {
    @NotBlank(message = "El tipo de nota es obligatorio")
    private String tipo;

    @NotNull @DecimalMin("0.0") @DecimalMax("5.0")
    private Double valor;

    @NotNull @DecimalMin("0.0") @DecimalMax("1.0")
    private Double porcentaje;
}
