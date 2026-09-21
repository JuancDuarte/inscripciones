package co.edu.uptc.inscripciones.repository;

import org.springframework.data.jpa.domain.Specification;

import co.edu.uptc.inscripciones.model.EstadoInscripcion;
import co.edu.uptc.inscripciones.model.Inscripcion;

public class InscripcionSpecifications {
    // Especificacion para filtrar inscripciones por diferentes criterios
    public static Specification<Inscripcion> porEstudianteId(Long estudianteId) {
        return (root, query, cb) ->
                estudianteId == null ? null : cb.equal(root.get("estudianteId"), estudianteId);
    }
    // Especificación para filtrar inscripciones por cursoId
    public static Specification<Inscripcion> porCursoId(Long cursoId) {
        return (root, query, cb) ->
                cursoId == null ? null : cb.equal(root.get("cursoId"), cursoId);
    }
    // Especificación para filtrar inscripciones por estado
    public static Specification<Inscripcion> porEstado(EstadoInscripcion estado) {
        return (root, query, cb) ->
                estado == null ? null : cb.equal(root.get("estado"), estado);
    }
    // Especificación para filtrar inscripciones por periodo
    public static Specification<Inscripcion> porPeriodo(String periodo) {
        return (root, query, cb) ->
                periodo == null || periodo.isBlank() ? null : cb.equal(root.get("periodo"), periodo);
    }

}
