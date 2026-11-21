package co.edu.uptc.inscripciones.exception;

/** 409: choca con el estado actual (sin cupo, ya inscrito). */
public class ConflictoException extends RuntimeException {
    private final String codigo;

    public ConflictoException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
