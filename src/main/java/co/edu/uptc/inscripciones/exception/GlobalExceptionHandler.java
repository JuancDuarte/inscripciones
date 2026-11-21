package co.edu.uptc.inscripciones.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import co.edu.uptc.inscripciones.model.EstadoInscripcion;
import co.edu.uptc.inscripciones.model.Inscripcion;
import co.edu.uptc.inscripciones.model.Nota;

@RestControllerAdvice 
public class GlobalExceptionHandler {
    @ExceptionHandler(RecursoNoEncontradoException.class)
   // Maneja la excepción de recurso no encontrado y devuelve una respuesta con el estado 404
    public ResponseEntity<ErrorResponse> handleNoEncontrado(RecursoNoEncontradoException ex) {
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .mensaje(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    // 409: sin cupo / ya inscrito
    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<ErrorResponse> handleConflicto(ConflictoException ex) {
        return respuesta(HttpStatus.CONFLICT, ex.getCodigo(), ex.getMessage());
    }

    // 409: la base rechazó un duplicado (restricción uq_insc)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleIntegridad(DataIntegrityViolationException ex) {
        return respuesta(HttpStatus.CONFLICT, "CONFLICTO", "La operación viola una restricción de la base de datos");
    }

    // 422: estudiante/curso inexistente o INACTIVO
    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorResponse> handleReglaNegocio(ReglaNegocioException ex) {
        return respuesta(HttpStatus.UNPROCESSABLE_ENTITY, ex.getCodigo(), ex.getMessage());
    }

    // 503: otro módulo no responde (nunca 500)
    @ExceptionHandler(ServicioNoDisponibleException.class)
    public ResponseEntity<ErrorResponse> handleNoDisponible(ServicioNoDisponibleException ex) {
        return respuesta(HttpStatus.SERVICE_UNAVAILABLE, "MODULO_NO_DISPONIBLE", ex.getMessage());
    }

    private ResponseEntity<ErrorResponse> respuesta(HttpStatus status, String codigo, String mensaje) {
        ErrorResponse error = ErrorResponse.builder()
                .status(status.value())
                .codigo(codigo)
                .mensaje(mensaje)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    // Maneja la excepción de validación de argumentos y devuelve una respuesta con el estado 400
    public ResponseEntity<ErrorResponse> handleValidacion(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(fe -> errores.put(fe.getField(), fe.getDefaultMessage()));

        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .mensaje("Error de validación")
                .timestamp(LocalDateTime.now())
                .errores(errores)
                .build();
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(Exception.class)
    // Maneja cualquier otra excepción no controlada y devuelve una respuesta con el estado 500
    public ResponseEntity<ErrorResponse> handleGenerico(Exception ex) {
        ErrorResponse error = ErrorResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .mensaje("Error interno: " + ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.internalServerError().body(error);
    }
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTipoInvalido(MethodArgumentTypeMismatchException ex) {
    ErrorResponse error = ErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .mensaje("Valor inválido para el parámetro '" + ex.getName() + "': " + ex.getValue())
            .timestamp(LocalDateTime.now())
            .build();
    return ResponseEntity.badRequest().body(error);
}
@ExceptionHandler(ParametrosInvalidos.class)
public ResponseEntity<ErrorResponse> handleParametrosInvalidos(ParametrosInvalidos ex) {
    ErrorResponse error = ErrorResponse.builder()
            .status(HttpStatus.BAD_REQUEST.value())
            .mensaje(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .build();
    return ResponseEntity.badRequest().body(error);
}

/**
 * El estudiante ya tiene una inscripción vigente en el mismo curso y periodo.
 * Se devuelve 409 para que el frontend muestre el mensaje exacto del conflicto.
 */
@ExceptionHandler(InscripcionDuplicadaException.class)
public ResponseEntity<ErrorResponse> handleDuplicada(InscripcionDuplicadaException ex) {
    ErrorResponse error = ErrorResponse.builder()
            .status(HttpStatus.CONFLICT.value())
            .mensaje(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .build();
    return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
}
}



