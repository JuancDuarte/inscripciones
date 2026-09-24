package co.edu.uptc.inscripciones.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import co.edu.uptc.inscripciones.dto.request.CambiarEstadoRequestDTO;
import co.edu.uptc.inscripciones.dto.request.InscripcionRequestDTO;
import co.edu.uptc.inscripciones.dto.response.InscripcionResponseDTO;
import co.edu.uptc.inscripciones.dto.response.PageResponseDTO;
import co.edu.uptc.inscripciones.exception.ParametrosInvalidos;
import co.edu.uptc.inscripciones.mapper.InscripcionMapper;
import co.edu.uptc.inscripciones.model.EstadoInscripcion;
import co.edu.uptc.inscripciones.model.Inscripcion;
import co.edu.uptc.inscripciones.repository.InscripcionSpecifications;
import co.edu.uptc.inscripciones.service.InscripcionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController 
@RequestMapping ("/api/inscripciones")
@RequiredArgsConstructor 
@Tag (name = "Inscripciones", description = "CRUD relacionada con las entidad inscripciones")
public class InscripcionController {
    private final InscripcionService inscripcionService;
    private final InscripcionMapper mapper;

    private static final Set<String> CAMPOS_ORDENABLES = Set.of(
            "idInscripcion", "estudianteId", "cursoId", "periodo", "estado", "fechaInscripcion"
    );
@GetMapping
public ResponseEntity<PageResponseDTO<InscripcionResponseDTO>> listar(
        @RequestParam(defaultValue = "0") int pageNumber,
        @RequestParam(defaultValue = "20") int pageSize,
        @RequestParam(defaultValue = "fechaInscripcion") String sortBy,
        @RequestParam(defaultValue = "desc") String sortDirection,
        @RequestParam(required = false) Long estudianteId,
        @RequestParam(required = false) Long cursoId,
        @RequestParam(required = false) EstadoInscripcion estado,
        @RequestParam(required = false) String periodo
) {
    if (pageSize != 10 && pageSize != 20 && pageSize != 50) pageSize = 20;

    Sort sort = construirSort(sortBy, sortDirection); // 👈 reemplaza la lógica de un solo campo
    PageRequest pageable = PageRequest.of(pageNumber, pageSize, sort);

    Specification<Inscripcion> spec = Specification
            .where(InscripcionSpecifications.porEstudianteId(estudianteId))
            .and(InscripcionSpecifications.porCursoId(cursoId))
            .and(InscripcionSpecifications.porEstado(estado))
            .and(InscripcionSpecifications.porPeriodo(periodo));

    Page<InscripcionResponseDTO> resultado = inscripcionService.listar(spec, pageable);
    return ResponseEntity.ok(PageResponseDTO.from(resultado));
}

    @GetMapping("/{id}")
    @Operation (summary = "Obtener inscripción por ID", description = "Permite obtener una inscripción específica por su ID")
    public ResponseEntity<InscripcionResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(inscripcionService.obtenerPorId(id));
    }

    @PostMapping
    @Operation (summary = "Crear inscripción", description = "Permite crear una nueva inscripción")
    public ResponseEntity<InscripcionResponseDTO> crear(@Valid @RequestBody InscripcionRequestDTO dto) {
    return ResponseEntity.status(HttpStatus.CREATED).body(inscripcionService.crear(dto));
    }

    @PutMapping("/{id}")
    @Operation (summary = "Actualizar inscripción", description = "Permite actualizar una inscripción existente")
    public ResponseEntity<InscripcionResponseDTO> actualizar(
            @PathVariable Long id, @Valid @RequestBody InscripcionRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.OK).body(inscripcionService.actualizar(id, dto));
    }

    @PatchMapping("/{id}/estado")
    @Operation (summary = "Cambiar estado de inscripción", description = "Permite cambiar el estado de una inscripción existente(ACTIVA, RETIRADA, FINALIZADA, CANCELADA)")
    public ResponseEntity<InscripcionResponseDTO> cambiarEstado(
            @PathVariable Long id, @Valid @RequestBody CambiarEstadoRequestDTO dto) {
        return ResponseEntity.ok(inscripcionService.cambiarEstado(id, dto.getNuevoEstado(), dto.getMotivo()));
    }

    @DeleteMapping("/{id}")
    @Operation (summary = "Eliminar inscripción", description = "Permite eliminar una inscripción existente")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        inscripcionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
    private Sort construirSort(String sortBy, String sortDirection) {
    String[] campos = sortBy.split(",");
    String[] direcciones = sortDirection.split(",");

    if (campos.length != direcciones.length) {
        throw new ParametrosInvalidos(
            "La cantidad de campos en sortBy (" + campos.length +
            ") no coincide con la cantidad en sortDirection (" + direcciones.length + ")"
        );
    }

    List<Sort.Order> ordenes = new ArrayList<>();
    for (int i = 0; i < campos.length; i++) {
        String campo = campos[i].trim();
        String direccion = direcciones[i].trim();

        if (!CAMPOS_ORDENABLES.contains(campo)) {
            throw new ParametrosInvalidos("Campo de ordenamiento inválido: '" + campo + "'");
        }
        if (!direccion.equalsIgnoreCase("asc") && !direccion.equalsIgnoreCase("desc")) {
            throw new ParametrosInvalidos(
                "Dirección de ordenamiento inválida: '" + direccion + "' (use 'asc' o 'desc')"
            );
        }

        Sort.Direction dir = direccion.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        ordenes.add(new Sort.Order(dir, campo));
    }

    return Sort.by(ordenes);
}
}
