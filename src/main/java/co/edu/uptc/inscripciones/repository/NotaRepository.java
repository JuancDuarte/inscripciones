package co.edu.uptc.inscripciones.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.uptc.inscripciones.model.Nota;

public interface NotaRepository extends JpaRepository<Nota, Long> {
    List<Nota> findByInscripcion_IdInscripcion(Long inscripcionId);
}
