package co.edu.uptc.inscripciones.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "inscripcion", uniqueConstraints = {
        // A2: no hay doble inscripción del mismo estudiante al mismo curso.
        @UniqueConstraint(name = "uq_insc", columnNames = {"estudiante_id", "curso_id"}),
        // A2: una Idempotency-Key solo puede crear UNA inscripción.
        @UniqueConstraint(name = "uq_insc_idempotency_key", columnNames = {"idempotency_key"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Inscripcion {
     @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idInscripcion;

    @NotNull(message = "El id del estudiante es obligatorio")
    @Column(nullable = false)
    private Long estudianteId;

    @NotNull(message = "El id del curso es obligatorio")
    @Column(nullable = false)
    private Long cursoId;

    @NotBlank(message = "El periodo es obligatorio")
    @Column(nullable = false)
    private String periodo;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoInscripcion estado;

    private LocalDate fechaInscripcion;

    /** Encabezado Idempotency-Key con el que se creó (null si no vino). */
    @Column(name = "idempotency_key", length = 100)
    private String idempotencyKey;

    @Builder.Default
    @OneToMany(mappedBy = "inscripcion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Nota> notas = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "inscripcion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HistorialEstado> historial = new ArrayList<>();
}


