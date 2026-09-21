package co.edu.uptc.inscripciones.seed;

import java.time.LocalDate;
import java.util.Locale;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import co.edu.uptc.inscripciones.model.EstadoInscripcion;
import co.edu.uptc.inscripciones.model.Inscripcion;
import co.edu.uptc.inscripciones.model.Nota;
import co.edu.uptc.inscripciones.repository.InscripcionRepository;
import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;

@Component 
@RequiredArgsConstructor 
public class DataSeeder implements CommandLineRunner {
private final InscripcionRepository inscripcionRepository;

    @Override
    public void run(String... args) {
        if (inscripcionRepository.count() > 0) return;

        Faker faker = new Faker(new Locale("es"));
        EstadoInscripcion[] estados = EstadoInscripcion.values();
        String[] periodos = {"2025-1", "2025-2", "2026-1", "2026-2"};

        for (int i = 0; i < 1000; i++) {
            Inscripcion inscripcion = Inscripcion.builder()
                    .estudianteId((long) faker.number().numberBetween(1, 20))
                    .cursoId((long) faker.number().numberBetween(1, 30))
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
        }
    }
}
