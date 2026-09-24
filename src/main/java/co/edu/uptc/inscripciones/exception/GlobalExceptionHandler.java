package co.edu.uptc.inscripciones.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.jpa.domain.Specification;
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
}



