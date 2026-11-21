package co.edu.uptc.inscripciones.seed;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import co.edu.uptc.inscripciones.model.EstadoInscripcion;
import co.edu.uptc.inscripciones.model.Inscripcion;
import co.edu.uptc.inscripciones.model.Nota;
import co.edu.uptc.inscripciones.repository.InscripcionRepository;
import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;

/**
 * Datos de ejemplo cuando la tabla está vacía. Genera pares (estudiante, curso)
 * ÚNICOS para respetar la restricción uq_insc. Se puede apagar con
 * SEED_INSCRIPCIONES=false (por ejemplo, cuando el simulador puebla la base).
 */
@Component
@Order(10)
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private static final int TOTAL = 300;

    private final InscripcionRepository inscripcionRepository;

    @Value("${seed.inscripciones:true}")
    private boolean habilitado;

    @Override
    public void run(String... args) {
        if (!habilitado || inscripcionRepository.count() > 0) return;

        @SuppressWarnings("deprecation")
        Faker faker = new Faker(new Locale("es"));
        EstadoInscripcion[] estados = EstadoInscripcion.values();
        String[] periodos = {"2025-1", "2025-2", "2026-1", "2026-2"};
        Set<String> usados = new HashSet<>();

        int creadas = 0;
        int intentos = 0;
        while (creadas < TOTAL && intentos++ < TOTAL * 10) {
            long estudianteId = faker.number().numberBetween(1, 20);
            long cursoId = faker.number().numberBetween(1, 30);
            if (!usados.add(estudianteId + "-" + cursoId)) continue;

            Inscripcion inscripcion = Inscripcion.builder()
                    .estudianteId(estudianteId)
                    .cursoId(cursoId)
                    .periodo(periodos[faker.number().numberBetween(0, periodos.length)])
                    .estado(estados[faker.number().numberBetween(0, estados.length)])
                    .fechaInscripcion(LocalDate.now().minusDays(faker.number().numberBetween(1, 365)))
                    .build();

            Nota nota = Nota.builder()
                    .tipo("Parcial 1")
                    .valor(faker.number().randomDouble(1, 0, 5))
                    .porcentaje(0.3)
                    .inscripcion(inscripcion)
                    .build();
            inscripcion.getNotas().add(nota);

            inscripcionRepository.save(inscripcion);
            creadas++;
        }
    }
}
