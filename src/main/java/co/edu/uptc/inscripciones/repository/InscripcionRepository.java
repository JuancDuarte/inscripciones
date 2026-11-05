package co.edu.uptc.inscripciones.repository;

import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import co.edu.uptc.inscripciones.model.EstadoInscripcion;
import co.edu.uptc.inscripciones.model.Inscripcion;

public interface InscripcionRepository extends JpaRepository<Inscripcion, Long>, JpaSpecificationExecutor<Inscripcion> {

    /**
     * ¿El estudiante ya tiene una inscripción en ese curso y periodo con alguno de esos estados?
     * Se usa para impedir duplicados vigentes (ACTIVA/FINALIZADA) al crear una inscripción.
     */
    boolean existsByEstudianteIdAndCursoIdAndPeriodoAndEstadoIn(
            Long estudianteId, Long cursoId, String periodo, Collection<EstadoInscripcion> estados);
}
