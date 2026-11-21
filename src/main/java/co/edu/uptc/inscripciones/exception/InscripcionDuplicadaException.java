package co.edu.uptc.inscripciones.exception;

/**
 * El estudiante ya tiene una inscripción vigente (ACTIVA o FINALIZADA) en el mismo curso y periodo.
 * Se responde 409 Conflict para que el frontend pueda mostrar un mensaje claro.
 */
public class InscripcionDuplicadaException extends RuntimeException {
    public InscripcionDuplicadaException(String mensaje) {
        super(mensaje);
    }
}
