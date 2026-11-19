package co.edu.uptc.inscripciones.seed;

import java.time.LocalDate;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import co.edu.uptc.inscripciones.model.EstadoInscripcion;
import co.edu.uptc.inscripciones.model.Inscripcion;
import co.edu.uptc.inscripciones.model.Nota;
import co.edu.uptc.inscripciones.repository.InscripcionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Datos de demostración del módulo Inscripciones.
 *
 * <p>Cada estudiante se inscribe únicamente en materias de su propio programa.
 * El módulo Materias siembra 7 pensums de 40 asignaturas (4 por semestre) y
 * oferta un curso por asignatura en 8 periodos. El orden es determinista:
 * el curso de la materia {@code m} en el periodo {@code p} tiene id
 * {@code p * 280 + m + 1}. Aquí se usa esa misma convención para no depender
 * de una llamada HTTP entre módulos durante el arranque.</p>
 *
 * <p>Los estudiantes 1 a 25 conservan el programa con el que los siembra el
 * módulo Estudiantes. Se les crea historial (FINALIZADA) y, si están activos,
 * matrícula del periodo actual (ACTIVA). Todas las notas son dos parciales
 * del 50% con valores entre 3.6 y 4.9, así el promedio siempre supera 3.5.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private static final int SEED_VERSION = 2;
    private static final String SEED_NAME = "inscripciones";

    private static final int MATERIAS_POR_PROGRAMA = 40;
    private static final int MATERIAS_POR_SEMESTRE = 4;
    private static final int MATERIAS_TOTALES = 7 * MATERIAS_POR_PROGRAMA; // 280
    private static final int PERIODO_ACTUAL = 7; // 2026-2

    private static final String[] PERIODOS = {
            "2023-1", "2023-2", "2024-1", "2024-2",
            "2025-1", "2025-2", "2026-1", "2026-2"
    };

    private static final List<String> PROGRAMAS = List.of(
            "ISIS", "IIND", "ICIV", "ADEM", "CPUB", "DERE", "TGRE");

    /**
     * Programa de cada estudiante, en el mismo orden del seeder de Estudiantes.
     * El índice 0 corresponde al estudiante 1.
     */
    private static final String[] PROGRAMA_POR_ESTUDIANTE = {
            "ISIS", "ISIS", "IIND", "ICIV", "ADEM", "CPUB", "ISIS", "DERE", "IIND", "ADEM",
            "ICIV", "ISIS", "ISIS", "CPUB", "IIND", "DERE", "ICIV", "ISIS", "ADEM", "IIND",
            "CPUB", "DERE", "ISIS", "ADEM", "ICIV"
    };

    /** Estudiantes inactivos, suspendidos o graduados: solo historial, sin matrícula actual. */
    private static final List<Integer> SIN_MATRICULA_ACTUAL = List.of(5, 7, 11, 16, 19);

    private final InscripcionRepository inscripcionRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS seed_control (
                    name    VARCHAR(50) PRIMARY KEY,
                    version INTEGER     NOT NULL
                )
                """);

        if (!necesitaReseed()) {
            log.info("Dataset de inscripciones al día (versión {}); se omite el seeding.", SEED_VERSION);
            return;
        }

        log.info("Dataset de inscripciones desactualizado: se reconstruye (versión {}).", SEED_VERSION);
        jdbcTemplate.execute("TRUNCATE TABLE nota, historial_estado, inscripcion RESTART IDENTITY CASCADE");

        int total = 0;
        for (int i = 0; i < PROGRAMA_POR_ESTUDIANTE.length; i++) {
            long estudianteId = i + 1L;
            int programaIndex = PROGRAMAS.indexOf(PROGRAMA_POR_ESTUDIANTE[i]);
            int semestresHistorial = 3 + (i % 4); // 3 a 6 semestres cursados
            int semestreActual = semestresHistorial + 1;

            for (int semestre = 1; semestre <= semestresHistorial; semestre++) {
                int periodoIndex = (PERIODO_ACTUAL - semestresHistorial) + (semestre - 1);
                String periodo = PERIODOS[periodoIndex];
                for (int j = 0; j < MATERIAS_POR_SEMESTRE; j++) {
                    long cursoId = cursoId(periodoIndex, programaIndex, semestre, j);
                    Inscripcion inscripcion = nuevaInscripcion(
                            estudianteId, cursoId, periodo, EstadoInscripcion.FINALIZADA, i, semestre, j);
                    inscripcionRepository.save(inscripcion);
                    total++;
                }
            }

            if (!SIN_MATRICULA_ACTUAL.contains((int) estudianteId) && semestreActual <= 10) {
                String periodo = PERIODOS[PERIODO_ACTUAL];
                for (int j = 0; j < MATERIAS_POR_SEMESTRE; j++) {
                    long cursoId = cursoId(PERIODO_ACTUAL, programaIndex, semestreActual, j);
                    Inscripcion inscripcion = nuevaInscripcion(
                            estudianteId, cursoId, periodo, EstadoInscripcion.ACTIVA, i, semestreActual, j);
                    inscripcionRepository.save(inscripcion);
                    total++;
                }
            }
        }

        jdbcTemplate.update("""
                INSERT INTO seed_control (name, version) VALUES (?, ?)
                ON CONFLICT (name) DO UPDATE SET version = EXCLUDED.version
                """, SEED_NAME, SEED_VERSION);
        log.info("Se sembraron {} inscripciones, todas dentro del programa del estudiante.", total);
    }

    private boolean necesitaReseed() {
        List<Integer> versiones = jdbcTemplate.query(
                "SELECT version FROM seed_control WHERE name = ?",
                (rs, rowNum) -> rs.getInt(1), SEED_NAME);
        return versiones.isEmpty() || versiones.get(0) < SEED_VERSION;
    }

    /** Id del curso de una materia en un periodo, según el orden determinista de Materias. */
    private long cursoId(int periodoIndex, int programaIndex, int semestre, int materiaEnSemestre) {
        int materiaIndex = programaIndex * MATERIAS_POR_PROGRAMA
                + (semestre - 1) * MATERIAS_POR_SEMESTRE
                + materiaEnSemestre;
        return (long) periodoIndex * MATERIAS_TOTALES + materiaIndex + 1L;
    }

    private Inscripcion nuevaInscripcion(long estudianteId, long cursoId, String periodo,
            EstadoInscripcion estado, int estudianteIndex, int semestre, int materiaEnSemestre) {
        Inscripcion inscripcion = Inscripcion.builder()
                .estudianteId(estudianteId)
                .cursoId(cursoId)
                .periodo(periodo)
                .estado(estado)
                .fechaInscripcion(fechaDePeriodo(periodo))
                .build();

        // Dos parciales del 50%. El rango 3.6-4.9 garantiza promedio > 3.5.
        double parcial1 = nota(estudianteIndex * 17 + semestre * 7 + materiaEnSemestre * 3);
        double parcial2 = nota(estudianteIndex * 11 + semestre * 5 + materiaEnSemestre * 7 + 5);
        inscripcion.getNotas().add(nota("Parcial 1", parcial1, inscripcion));
        inscripcion.getNotas().add(nota("Parcial 2", parcial2, inscripcion));
        return inscripcion;
    }

    private Nota nota(String tipo, double valor, Inscripcion inscripcion) {
        return Nota.builder()
                .tipo(tipo)
                .valor(valor)
                .porcentaje(0.5)
                .inscripcion(inscripcion)
                .build();
    }

    private double nota(int semilla) {
        int paso = Math.floorMod(semilla, 14); // 0..13
        return 3.6 + paso / 10.0;              // 3.6..4.9
    }

    private LocalDate fechaDePeriodo(String periodo) {
        int anio = Integer.parseInt(periodo.substring(0, 4));
        int mes = periodo.endsWith("-1") ? 2 : 8;
        return LocalDate.of(anio, mes, 15);
    }
}
