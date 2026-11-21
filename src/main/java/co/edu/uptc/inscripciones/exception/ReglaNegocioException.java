package co.edu.uptc.inscripciones.exception;

/** 422: la solicitud está bien formada pero viola una regla (no existe, está INACTIVO...). */
public class ReglaNegocioException extends RuntimeException {
    private final String codigo;

    public ReglaNegocioException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
