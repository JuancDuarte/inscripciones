package co.edu.uptc.inscripciones.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Configuración global del módulo. Solo existe una fila (id = 1) y controla si
 * los estudiantes pueden crear inscripciones en este momento.
 */
@Entity
@Table(name = "configuracion_inscripcion")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ConfiguracionInscripcion {

    public static final Long ID_UNICO = 1L;

    @Id
    private Long id;

    @Column(name = "habilitada", nullable = false)
    private Boolean habilitada;

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;
}
