package co.edu.uptc.inscripciones.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Migración idempotente que corre antes del seeder. La base de la nube ya
 * tiene inscripciones duplicadas (el seeder anterior las creaba), y con
 * duplicados no se puede crear la restricción única. Entonces:
 *   1. se borran los duplicados de (estudiante_id, curso_id) dejando el menor id
 *      (con sus notas e historial),
 *   2. se crea la restricción uq_insc si todavía no existe.
 */
@Component
@Order(0)
@RequiredArgsConstructor
@Slf4j
public class ConsistenciaMigracion implements ApplicationRunner {

    private static final String DUPLICADOS = """
            SELECT id_inscripcion FROM (
              SELECT id_inscripcion,
                     row_number() OVER (PARTITION BY estudiante_id, curso_id ORDER BY id_inscripcion) AS n
                FROM inscripcion
            ) t WHERE t.n > 1
            """;

    private final JdbcTemplate jdbc;
    private final TransactionTemplate tx;

    @Override
    public void run(ApplicationArguments args) {
        tx.executeWithoutResult(status -> {
            jdbc.update("DELETE FROM nota WHERE inscripcion_id IN (" + DUPLICADOS + ")");
            jdbc.update("DELETE FROM historial_estado WHERE inscripcion_id IN (" + DUPLICADOS + ")");
            int borradas = jdbc.update("DELETE FROM inscripcion WHERE id_inscripcion IN (" + DUPLICADOS + ")");
            if (borradas > 0) {
                log.warn("Migración: se eliminaron {} inscripciones duplicadas antes de crear uq_insc", borradas);
            }
            jdbc.execute("""
                    DO $$
                    BEGIN
                      IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uq_insc') THEN
                        ALTER TABLE inscripcion ADD CONSTRAINT uq_insc UNIQUE (estudiante_id, curso_id);
                      END IF;
                      IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uq_insc_idempotency_key') THEN
                        ALTER TABLE inscripcion ADD CONSTRAINT uq_insc_idempotency_key UNIQUE (idempotency_key);
                      END IF;
                    END $$;
                    """);
        });
    }
}
