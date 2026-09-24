# Módulo Inscripciones — Lab 3 (Arquitectura Modular con API Gateway)

Servicio independiente del módulo Inscripciones del Sistema Académico: contiene las entidades `Inscripcion` (raíz), `Nota` y `HistorialEstado`. Es uno de los tres módulos que el API Gateway del equipo expone bajo un único punto de entrada.

## Independencia del módulo

- **Repositorio propio**: este proyecto se clona y corre solo, sin código de los módulos Estudiantes o Materias.
- **Base de datos propia**: PostgreSQL en su propio contenedor Docker (`inscripciones-db`), sin foreign keys hacia las bases de datos de los otros módulos. `estudianteId` y `cursoId` en `Inscripcion` son solo números (ids externos) — nunca relaciones `@ManyToOne` reales, porque `Estudiante` y `Curso` no viven en esta base de datos. Cuando este módulo necesita el nombre de un estudiante o los datos de un curso, se los pide por HTTP en tiempo de ejecución al módulo correspondiente — nunca los duplica.
- **Ciclo de vida propio**: se levanta, se cae y se reinicia sin tocar a los otros dos.

Dentro del módulo, `Nota` e `HistorialEstado` sí tienen relaciones reales (`@ManyToOne`) hacia `Inscripcion`, porque las tres viven en la misma base de datos — eso es distinto del caso de un id "externo" hacia otro módulo.

## Stack

- Java 23 + Spring Boot 4.1.1 (Spring Framework 7)
- Spring Data JPA (Hibernate 7.4.5)
- PostgreSQL 16 (contenedor Docker)
- Bean Validation (jakarta.validation)
- springdoc-openapi 3.0.0 (Swagger UI, compatible con Spring Boot 4 / Framework 7)
- Datafaker (carga inicial de datos ficticios)
- Lombok

## 1. Requisitos

- JDK 23+
- Maven 3.9+
- Docker + Docker Compose

## 2. Levantar la base de datos

```bash
docker compose up -d
```

Levanta PostgreSQL en `localhost:5434` (host) -> `5432` (contenedor), con:

- DB: `inscripciones_db`
- Usuario: `postgres`
- Password: `postgres`

El puerto host `5434` se eligió para no chocar con las bases de datos de los módulos Estudiantes (`5433`) ni Materias (`5432`) cuando los 4 procesos corren al tiempo en la misma máquina. Ya está configurado en `src/main/resources/application.yml`.

## 3. Ejecutar la aplicación

```bash
mvn spring-boot:run
```

Al arrancar por primera vez:

- Hibernate crea automáticamente las tablas (`spring.jpa.hibernate.ddl-auto=update`).
- El `DataSeeder` puebla la base con 1000 inscripciones ficticias (con su respectiva nota inicial) solo si las tablas están vacías, no duplica en reinicios.

La API queda disponible en `http://localhost:8083`.

Ajusta `server.port` en `application.yml` si tu equipo acordó otros puertos para Estudiantes / Materias / Gateway y hay choque.

## 4. Documentación / pruebas (Swagger)

- Swagger UI: `http://localhost:8082/swagger-ui.html`
- Contrato OpenAPI (JSON): `http://localhost:8082/v3/api-docs`

Desde Swagger UI puedes usar "Try it out" para probar cada endpoint sin leer el código fuente. Hay 3 grupos de endpoints (Inscripciones, Notas, Historial de Estado), todos bajo el prefijo `/api/inscripciones`, que es el que el Gateway enruta hacia este servicio.

## 5. Endpoints principales

| Recurso | Base |
|---|---|
| Inscripciones | `/api/inscripciones` |
| Notas | `/api/inscripciones/{inscripcionId}/notas` |
| Historial de Estado | `/api/inscripciones/{inscripcionId}/historial` |

`Inscripcion` soporta:

- `GET` (lista paginada) — `pageNumber`, `pageSize`, `sortBy`, `sortDirection`.
- `GET /{id}`, `POST`, `PUT /{id}`, `PATCH /{id}/estado`, `DELETE /{id}`.

`Nota` soporta CRUD completo (`GET`, `GET /{id}`, `POST`, `PUT /{id}`, `DELETE /{id}`) anidado bajo su inscripción.

`HistorialEstado` solo expone `GET` (lista, por id) y `DELETE` — es un registro de auditoría que se genera automáticamente al cambiar el estado de una inscripción (`PATCH /{id}/estado`), nunca se crea ni edita a mano.

### Filtros disponibles

**Inscripciones** (`GET /api/inscripciones`)

- `estudianteId` — inscripciones de un estudiante puntual (es el filtro que consume el Gateway).
- `cursoId` — inscripciones de un curso puntual.
- `estado` — `ACTIVA`, `RETIRADA`, `FINALIZADA` o `CANCELADA`.
- `periodo` — coincidencia exacta (ej. `2026-2`).

### Endpoint que consume el Gateway

`GET /api/inscripciones?estudianteId={id}` devuelve las inscripciones de un estudiante, cada una con sus `notas` ya incluidas. Es el que el Gateway llama en el paso 2 de `/api/estudiantes/{id}/detalle` (ver sección 6.1 del enunciado del laboratorio); para cada inscripción obtenida, el Gateway hace las llamadas siguientes al módulo Materias usando el `cursoId` que trae cada resultado.

## 6. Manejo de errores

Respuestas de error consistentes vía `GlobalExceptionHandler`:

- `400` — validación fallida o parámetro con formato inválido (incluye enums inválidos, ej. `estado=NOEXISTE`).
- `404` — recurso (inscripción, nota o historial) inexistente.
- `500` — error inesperado.

## 7. Estructura del código

```
src/main/java/co/edu/uptc/inscripciones/
├── InscripcionesApplication.java
├── config/OpenApiConfig.java
├── model/                (Inscripcion, Nota, HistorialEstado, EstadoInscripcion)
├── dto/
│   ├── request/          (InscripcionRequestDTO, NotaRequestDTO, CambiarEstadoRequestDTO)
│   └── response/         (InscripcionResponseDTO, NotaResponseDTO, HistorialEstadoResponseDTO, PageResponseDTO)
├── mapper/InscripcionMapper.java
├── repository/           (InscripcionRepository, NotaRepository, HistorialEstadoRepository)
│   └── specification/InscripcionSpecifications.java
├── service/              (InscripcionService, NotaService, HistorialEstadoService)
│   └── implement/        (InscripcionServiceImpl, NotaServiceImpl, HistorialEstadoServiceImpl)
├── controller/           (InscripcionController, NotaController, HistorialEstadoController)
├── exception/            (RecursoNoEncontradoException, ErrorResponse, GlobalExceptionHandler)
└── seed/DataSeeder.java
```

**Nota de diseño**: los mappers a DTO se ejecutan dentro de los métodos `@Transactional` de cada service, nunca en el controller — así se evita `LazyInitializationException` al serializar las colecciones `notas`/`historial` de `Inscripcion`. La comunicación entre `Service`s del mismo módulo (por ejemplo, `InscripcionServiceImpl` → `HistorialEstadoService.registrarCambio`) se hace con entidades JPA, no con DTOs; los DTOs son solo la frontera hacia el `Controller`.
