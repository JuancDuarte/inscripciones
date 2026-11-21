package co.edu.uptc.inscripciones.service.implement;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import co.edu.uptc.inscripciones.client.EstudiantesClient;
import co.edu.uptc.inscripciones.client.MateriasClient;
import co.edu.uptc.inscripciones.dto.request.InscripcionRequestDTO;
import co.edu.uptc.inscripciones.dto.response.InscripcionResponseDTO;
import co.edu.uptc.inscripciones.exception.ConflictoException;
import co.edu.uptc.inscripciones.exception.RecursoNoEncontradoException;
import co.edu.uptc.inscripciones.exception.ReglaNegocioException;
import co.edu.uptc.inscripciones.mapper.InscripcionMapper;
import co.edu.uptc.inscripciones.model.EstadoInscripcion;
import co.edu.uptc.inscripciones.model.Inscripcion;
import co.edu.uptc.inscripciones.repository.InscripcionRepository;
import co.edu.uptc.inscripciones.service.HistorialEstadoService;
import co.edu.uptc.inscripciones.service.InscripcionService;
import co.edu.uptc.inscripciones.service.ResultadoCreacion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class InscripcionServiceImpl implements InscripcionService {

    /** Estados que ocupan un cupo en el curso. */
    private static final Set<EstadoInscripcion> OCUPAN_CUPO = EnumSet.of(EstadoInscripcion.ACTIVA, EstadoInscripcion.FINALIZADA);
    private static final Set<EstadoInscripcion> LIBERADOS = EnumSet.of(EstadoInscripcion.CANCELADA, EstadoInscripcion.RETIRADA);

    private final InscripcionRepository inscripcionRepository;
    private final HistorialEstadoService historialEstadoService;
    private final InscripcionMapper mapper;
    private final EstudiantesClient estudiantesClient;
    private final MateriasClient materiasClient;
    private final TransactionTemplate tx;

    @Override
    public Page<InscripcionResponseDTO> listar(Specification<Inscripcion> spec, Pageable pageable) {
        return inscripcionRepository.findAll(spec, pageable).map(mapper::toResponseDTO);
    }

    @Override
    public InscripcionResponseDTO obtenerPorId(Long id) {
        return mapper.toResponseDTO(buscar(id));
    }

    /**
     * A2 · Crear una inscripción sin llaves foráneas que nos cuiden:
     *  1. Idempotency-Key repetida -> se devuelve la inscripción anterior.
     *  2. Ya inscrito -> 409 (la restricción uq_insc es la garantía real).
     *  3. Estudiantes: debe existir y estar ACTIVO (422), o 503 si no responde.
     *  4. Materias: reserva el cupo con una sola sentencia condicional
     *     (409 sin cupo, 422 curso inexistente o INACTIVO, 503 caído).
     *  5. Se guarda la fila. Si falla, se libera el cupo (compensación).
     *
     * No corre dentro de una transacción larga: las llamadas HTTP no deben
     * mantener abierta una conexión a la base. Cada escritura es atómica.
     */
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public ResultadoCreacion crear(InscripcionRequestDTO dto, String idempotencyKey) {
        String key = (idempotencyKey == null || idempotencyKey.isBlank()) ? null : idempotencyKey.trim();
        if (key != null && key.length() > 100) {
            throw new ReglaNegocioException("IDEMPOTENCY_KEY_INVALIDA", "La Idempotency-Key no puede superar 100 caracteres");
        }

        // 1. Repetición de la misma solicitud
        if (key != null) {
            Optional<InscripcionResponseDTO> previa = porKey(key, dto);
            if (previa.isPresent()) return new ResultadoCreacion(previa.get(), true);
        }

        // 2. Ya existe una inscripción para (estudiante, curso)
        Optional<Inscripcion> existente = tx.execute(s ->
                inscripcionRepository.findByEstudianteIdAndCursoId(dto.getEstudianteId(), dto.getCursoId()));
        if (existente.isPresent() && OCUPAN_CUPO.contains(existente.get().getEstado())) {
            throw new ConflictoException("YA_INSCRITO", "El estudiante " + dto.getEstudianteId()
                    + " ya está inscrito en el curso " + dto.getCursoId());
        }

        // 3. Validar el estudiante en su módulo (antes de tocar el cupo)
        estudiantesClient.validarActivo(dto.getEstudianteId());

        // 4. Reservar el cupo (atómico en Materias)
        materiasClient.reservarCupo(dto.getCursoId());

        // 5. Guardar; si algo falla, devolver el cupo
        try {
            if (existente.isPresent()) {
                return new ResultadoCreacion(reactivar(existente.get(), dto), false);
            }
            return new ResultadoCreacion(guardarNueva(dto, key), false);
        } catch (DataIntegrityViolationException e) {
            materiasClient.liberarCupo(dto.getCursoId());
            String causa = String.valueOf(e.getMostSpecificCause().getMessage());
            if (key != null && causa.contains("uq_insc_idempotency_key")) {
                // Otra solicitud con la misma key ganó la carrera: devolver la suya.
                return porKey(key, dto).map(r -> new ResultadoCreacion(r, true))
                        .orElseThrow(() -> new ConflictoException("YA_INSCRITO", "Inscripción duplicada"));
            }
            throw new ConflictoException("YA_INSCRITO", "El estudiante " + dto.getEstudianteId()
                    + " ya está inscrito en el curso " + dto.getCursoId());
        } catch (RuntimeException e) {
            materiasClient.liberarCupo(dto.getCursoId());
            throw e;
        }
    }

    private InscripcionResponseDTO guardarNueva(InscripcionRequestDTO dto, String key) {
        return tx.execute(s -> {
            Inscripcion inscripcion = mapper.toEntity(dto);
            inscripcion.setEstado(EstadoInscripcion.ACTIVA);
            inscripcion.setFechaInscripcion(LocalDate.now());
            inscripcion.setIdempotencyKey(key);
            return mapper.toResponseDTO(inscripcionRepository.saveAndFlush(inscripcion));
        });
    }

    /** Una inscripción CANCELADA o RETIRADA se reactiva en lugar de crear otra fila (uq_insc). */
    private InscripcionResponseDTO reactivar(Inscripcion anterior, InscripcionRequestDTO dto) {
        return tx.execute(s -> {
            int filas = inscripcionRepository.reactivar(anterior.getIdInscripcion(), dto.getPeriodo(), LIBERADOS);
            if (filas == 0) {
                throw new ConflictoException("YA_INSCRITO", "El estudiante ya está inscrito en el curso " + dto.getCursoId());
            }
            Inscripcion actualizada = buscar(anterior.getIdInscripcion());
            historialEstadoService.registrarCambio(actualizada, anterior.getEstado(), EstadoInscripcion.ACTIVA,
                    "Reinscripción");
            return mapper.toResponseDTO(actualizada);
        });
    }

    private Optional<InscripcionResponseDTO> porKey(String key, InscripcionRequestDTO dto) {
        return tx.execute(s -> inscripcionRepository.findByIdempotencyKey(key).map(previa -> {
            if (!Objects.equals(previa.getEstudianteId(), dto.getEstudianteId())
                    || !Objects.equals(previa.getCursoId(), dto.getCursoId())) {
                throw new ReglaNegocioException("IDEMPOTENCY_KEY_REUTILIZADA",
                        "La Idempotency-Key ya se usó para otra inscripción");
            }
            return mapper.toResponseDTO(previa);
        }));
    }

    /**
     * Cambiar estudiante o curso implicaría mover cupos entre cursos: para eso
     * se cancela y se crea una inscripción nueva. Aquí solo cambian el período
     * y el estado.
     */
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public InscripcionResponseDTO actualizar(Long id, InscripcionRequestDTO dto) {
        Inscripcion actual = tx.execute(s -> buscar(id));
        if (!Objects.equals(actual.getEstudianteId(), dto.getEstudianteId())
                || !Objects.equals(actual.getCursoId(), dto.getCursoId())) {
            throw new ReglaNegocioException("CAMBIO_NO_PERMITIDO",
                    "No se puede cambiar el estudiante ni el curso: cancele y cree una nueva inscripción");
        }
        tx.executeWithoutResult(s -> {
            Inscripcion i = buscar(id);
            i.setPeriodo(dto.getPeriodo());
            inscripcionRepository.save(i);
        });
        if (dto.getEstado() != null && dto.getEstado() != actual.getEstado()) {
            return cambiarEstado(id, dto.getEstado(), "Actualización");
        }
        return tx.execute(s -> mapper.toResponseDTO(buscar(id)));
    }

    /**
     * Mantiene el cupo coherente con el estado:
     *  - pasar a CANCELADA/RETIRADA libera el cupo (después de guardar),
     *  - volver a ACTIVA/FINALIZADA lo reserva primero (409 si ya no hay).
     */
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public InscripcionResponseDTO cambiarEstado(Long id, EstadoInscripcion nuevoEstado, String motivo) {
        Inscripcion actual = tx.execute(s -> buscar(id));
        EstadoInscripcion anterior = actual.getEstado();
        boolean ocupaba = OCUPAN_CUPO.contains(anterior);
        boolean ocupara = OCUPAN_CUPO.contains(nuevoEstado);

        if (!ocupaba && ocupara) {
            estudiantesClient.validarActivo(actual.getEstudianteId());
            materiasClient.reservarCupo(actual.getCursoId());
        }
        InscripcionResponseDTO resultado;
        try {
            resultado = tx.execute(s -> {
                Inscripcion i = buscar(id);
                i.setEstado(nuevoEstado);
                Inscripcion guardada = inscripcionRepository.save(i);
                historialEstadoService.registrarCambio(guardada, anterior, nuevoEstado, motivo);
                return mapper.toResponseDTO(guardada);
            });
        } catch (RuntimeException e) {
            if (!ocupaba && ocupara) materiasClient.liberarCupo(actual.getCursoId());
            throw e;
        }
        if (ocupaba && !ocupara) {
            materiasClient.liberarCupo(actual.getCursoId());
        }
        return resultado;
    }

    /** A1: no se borra la fila; queda CANCELADA (con historial) y se libera el cupo. */
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void eliminar(Long id) {
        Inscripcion actual = tx.execute(s -> buscar(id));
        if (actual.getEstado() != EstadoInscripcion.CANCELADA) {
            cambiarEstado(id, EstadoInscripcion.CANCELADA, "Eliminada (borrado lógico)");
        }
    }

    private Inscripcion buscar(Long id) {
        return inscripcionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Inscripción no encontrada con id: " + id));
    }
}
