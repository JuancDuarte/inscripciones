package co.edu.uptc.inscripciones.exception;

/** 503: otro módulo (Estudiantes o Materias) no respondió. Nunca se traduce a 500. */
public class ServicioNoDisponibleException extends RuntimeException {
    private final String modulo;

    public ServicioNoDisponibleException(String modulo, Throwable causa) {
        super("El módulo " + modulo + " no está disponible, intente más tarde", causa);
        this.modulo = modulo;
    }

    public String getModulo() {
        return modulo;
    }
}
