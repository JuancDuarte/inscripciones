package co.edu.uptc.inscripciones.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import co.edu.uptc.inscripciones.model.ConfiguracionInscripcion;

public interface ConfiguracionInscripcionRepository extends JpaRepository<ConfiguracionInscripcion, Long> {
}
