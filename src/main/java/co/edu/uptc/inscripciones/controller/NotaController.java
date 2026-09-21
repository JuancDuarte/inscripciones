package co.edu.uptc.inscripciones.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import co.edu.uptc.inscripciones.dto.request.NotaRequestDTO;
import co.edu.uptc.inscripciones.dto.response.NotaResponseDTO;
import co.edu.uptc.inscripciones.mapper.InscripcionMapper;
import co.edu.uptc.inscripciones.model.Nota;
import co.edu.uptc.inscripciones.service.NotaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/inscripciones/{inscripcionId}/notas")
@RequiredArgsConstructor 
@Tag (name = "Notas", description = "CRUD relacionada con las entidad notas")
public class NotaController {
    private final NotaService notaService;
    private final InscripcionMapper mapper;

    @GetMapping
    @Operation (summary = "Listar notas de una inscripción", description = "Permite listar todas las notas asociadas a una inscripción específica")
    public ResponseEntity<List<NotaResponseDTO>> listar(@PathVariable Long inscripcionId) {
        return ResponseEntity.ok(notaService.listarPorInscripcion(inscripcionId));
    }

    @GetMapping("/{notaId}")
    @Operation (summary = "Obtener nota por ID", description = "Permite obtener una nota específica asociada a una inscripción")
    public ResponseEntity<NotaResponseDTO> obtenerPorId(@PathVariable Long inscripcionId, @PathVariable Long notaId) {
        return ResponseEntity.ok(notaService.obtenerPorId(notaId));
    }

    @PostMapping
    @Operation (summary = "Crear nota para una inscripción", description = "Permite crear una nueva nota asociada a una inscripción específica")
    public ResponseEntity<NotaResponseDTO> crear(
    @PathVariable Long inscripcionId, @Valid @RequestBody NotaRequestDTO dto) {
    return ResponseEntity.status(HttpStatus.CREATED).body(notaService.crear(inscripcionId, dto));
    }

    @PutMapping("/{notaId}")
    @Operation (summary = "Actualizar nota de una inscripción", description = "Permite actualizar una nota existente asociada a una inscripción específica")
    public ResponseEntity<NotaResponseDTO> actualizar(
            @PathVariable Long inscripcionId, @PathVariable Long notaId, @Valid @RequestBody NotaRequestDTO dto) {
        return ResponseEntity.ok(notaService.actualizar(notaId, dto));
    }

    @DeleteMapping("/{notaId}")
    @Operation (summary = "Eliminar nota de una inscripción", description = "Permite eliminar una nota asociada a una inscripción específica")
    public ResponseEntity<Void> eliminar(@PathVariable Long inscripcionId, @PathVariable Long notaId) {
        notaService.eliminar(notaId);
        return ResponseEntity.noContent().build();
    }

}
