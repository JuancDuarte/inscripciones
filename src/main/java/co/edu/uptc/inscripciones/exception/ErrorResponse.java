package co.edu.uptc.inscripciones.exception;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Builder 
public class ErrorResponse {
     private int status;
    private String mensaje;
    private LocalDateTime timestamp;
    private Map<String, String> errores; // solo se usa en errores de validación

}
