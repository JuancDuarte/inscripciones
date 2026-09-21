package co.edu.uptc.inscripciones.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Builder
public class NotaResponseDTO {
    private Long idNota;
    private String tipo;
    private Double valor;
    private Double porcentaje;
}