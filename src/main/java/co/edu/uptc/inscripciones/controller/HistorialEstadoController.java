package co.edu.uptc.inscripciones.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.uptc.inscripciones.dto.response.HistorialEstadoResponseDTO;
import co.edu.uptc.inscripciones.mapper.InscripcionMapper;
import co.edu.uptc.inscripciones.service.HistorialEstadoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/inscripciones/{inscripcionId}/historial")
@RequiredArgsConstructor
@Tag(name = "Historial de Estado", description = "CRUD relacionada con el historial de estado de las inscripciones")
public class HistorialEstadoController {
    private final HistorialEstadoService historialEstadoService;
    private final InscripcionMapper mapper;

    @GetMapping
    @Operation (summary = "Listar historial de estado de una inscripción", description = "Permite listar todos los cambios de estado asociados a una inscripción específica")
    public ResponseEntity<List<HistorialEstadoResponseDTO>> listar(@PathVariable Long inscripcionId) {
        return ResponseEntity.ok(historialEstadoService.listarPorInscripcion(inscripcionId));
    }

    @GetMapping("/{historialId}")
    @Operation (summary = "Obtener historial de estado por ID", description = "Permite obtener un cambio de estado específico asociado a una inscripción")
    public ResponseEntity<HistorialEstadoResponseDTO> obtenerPorId(
            @PathVariable Long inscripcionId, @PathVariable Long historialId) {
        return ResponseEntity.ok(historialEstadoService.obtenerPorId(historialId));
    }

    // Sin POST/PUT directo — se crea únicamente vía PATCH /inscripciones/{id}/estado (ver justificación arriba)

    @DeleteMapping("/{historialId}")
    @Operation (summary = "Eliminar historial de estado por ID", description = "Permite eliminar un cambio de estado específico asociado a una inscripción")
    public ResponseEntity<Void> eliminar(@PathVariable Long inscripcionId, @PathVariable Long historialId) {
        historialEstadoService.eliminar(historialId);
        return ResponseEntity.noContent().build();
    }

}
