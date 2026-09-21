package co.edu.uptc.inscripciones.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.uptc.inscripciones.model.HistorialEstado;

public interface HistorialEstadoRepository extends JpaRepository<HistorialEstado, Long> {
    List<HistorialEstado> findByInscripcion_IdInscripcion(Long inscripcionId);
}
