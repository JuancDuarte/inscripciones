package co.edu.uptc.inscripciones.repository;

import java.util.Collection;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import co.edu.uptc.inscripciones.model.EstadoInscripcion;
import co.edu.uptc.inscripciones.model.Inscripcion;

public interface InscripcionRepository extends JpaRepository<Inscripcion, Long>, JpaSpecificationExecutor<Inscripcion> {

    Optional<Inscripcion> findByIdempotencyKey(String idempotencyKey);

    Optional<Inscripcion> findByEstudianteIdAndCursoId(Long estudianteId, Long cursoId);

    /**
     * Reactiva una inscripción cancelada/retirada con una sentencia condicional:
     * si dos solicitudes llegan a la vez, solo una cambia la fila.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE Inscripcion i
               SET i.estado = co.edu.uptc.inscripciones.model.EstadoInscripcion.ACTIVA,
                   i.periodo = :periodo
             WHERE i.idInscripcion = :id AND i.estado IN :desde
            """)
    int reactivar(@Param("id") Long id, @Param("periodo") String periodo,
                  @Param("desde") Collection<EstadoInscripcion> desde);

    /**
     * ¿El estudiante ya tiene una inscripción en ese curso y periodo con alguno de esos estados?
     * Se usa para impedir duplicados vigentes (ACTIVA/FINALIZADA) al crear una inscripción.
     */
    boolean existsByEstudianteIdAndCursoIdAndPeriodoAndEstadoIn(
            Long estudianteId, Long cursoId, String periodo, Collection<EstadoInscripcion> estados);
}
