package co.edu.uptc.inscripciones.service.implement;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uptc.inscripciones.dto.response.ConfiguracionInscripcionResponseDTO;
import co.edu.uptc.inscripciones.model.ConfiguracionInscripcion;
import co.edu.uptc.inscripciones.repository.ConfiguracionInscripcionRepository;
import co.edu.uptc.inscripciones.service.ConfiguracionInscripcionService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConfiguracionInscripcionServiceImpl implements ConfiguracionInscripcionService {

    private final ConfiguracionInscripcionRepository repository;

    @Override
    @Transactional(readOnly = true)
    public ConfiguracionInscripcionResponseDTO obtener() {
        return repository.findById(ConfiguracionInscripcion.ID_UNICO)
                .map(this::aDto)
                .orElseGet(() -> new ConfiguracionInscripcionResponseDTO(true, null));
    }

    @Override
    @Transactional
    public ConfiguracionInscripcionResponseDTO cambiar(boolean habilitada) {
        ConfiguracionInscripcion configuracion = repository.findById(ConfiguracionInscripcion.ID_UNICO)
                .orElseGet(() -> ConfiguracionInscripcion.builder()
                        .id(ConfiguracionInscripcion.ID_UNICO)
                        .build());
        configuracion.setHabilitada(habilitada);
        configuracion.setActualizadoEn(LocalDateTime.now());
        return aDto(repository.save(configuracion));
    }

    private ConfiguracionInscripcionResponseDTO aDto(ConfiguracionInscripcion configuracion) {
        return new ConfiguracionInscripcionResponseDTO(
                Boolean.TRUE.equals(configuracion.getHabilitada()),
                configuracion.getActualizadoEn());
    }
}
