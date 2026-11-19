package co.edu.uptc.inscripciones.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.uptc.inscripciones.dto.request.CambiarConfiguracionInscripcionRequestDTO;
import co.edu.uptc.inscripciones.dto.response.ConfiguracionInscripcionResponseDTO;
import co.edu.uptc.inscripciones.service.ConfiguracionInscripcionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Interruptor global que decide si los estudiantes pueden inscribir materias.
 * El Gateway lo consulta antes de aceptar POST /api/inscripciones/mias y el
 * panel de administración lo cambia con PUT.
 */
@RestController
@RequestMapping("/api/inscripciones/configuracion")
@RequiredArgsConstructor
@Tag(name = "Configuración de inscripciones", description = "Ventana de inscripción de materias")
public class ConfiguracionInscripcionController {

    private final ConfiguracionInscripcionService service;

    @GetMapping
    @Operation(summary = "Consultar si la inscripción de materias está habilitada")
    public ResponseEntity<ConfiguracionInscripcionResponseDTO> obtener() {
        return ResponseEntity.ok(service.obtener());
    }

    @PutMapping
    @Operation(summary = "Habilitar o cerrar la inscripción de materias")
    public ResponseEntity<ConfiguracionInscripcionResponseDTO> cambiar(
            @Valid @RequestBody CambiarConfiguracionInscripcionRequestDTO dto) {
        return ResponseEntity.ok(service.cambiar(dto.habilitada()));
    }
}
