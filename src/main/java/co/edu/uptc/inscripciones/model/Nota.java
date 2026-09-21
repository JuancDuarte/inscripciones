package co.edu.uptc.inscripciones.model;

import co.edu.uptc.inscripciones.dto.response.InscripcionResponseDTO;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "nota")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Nota {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idNota;

    @NotBlank
    private String tipo;

    @NotNull @DecimalMin("0.0") @DecimalMax("5.0")
    private Double valor;

    @NotNull @DecimalMin("0.0") @DecimalMax("1.0")
    private Double porcentaje;

    @ManyToOne
    @JoinColumn(name = "inscripcion_id", nullable = false)
    private Inscripcion inscripcion;
}


