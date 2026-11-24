# javatest

Proyecto de pruebas en Spring Boot para la gestion de salas y reservas.

## Requisitos implementados

1. Crear y listar salas. Crear, listar y cancelar reservas. Cada reserva incluye sala, responsable, inicio y fin.
2. Si la sala ya esta ocupada en el horario solicitado, la API responde `409 Conflict`. Las validaciones de datos responden `400 Bad Request` con detalles claros.
3. Listado de reservas con filtros por sala y por fecha, con paginacion.
4. Base de datos H2 por defecto con migraciones Flyway. Tambien se incluye driver PostgreSQL para poder configurar una base PostgreSQL.
5. Los endpoints que crean o cancelan requieren autenticacion HTTP Basic; los endpoints de consulta son publicos.
6. Pruebas automatizadas: 2 unitarias y 1 de integracion con `MockMvc` y contexto completo de Spring Boot.

## Como ejecutarlo

### Requisitos previos

- Java 17+ (`JAVA_HOME` configurado)
- Maven Wrapper incluido en el proyecto (`mvnw.cmd` en Windows, `mvnw` en Linux/macOS); no requiere Maven instalado globalmente.

### Ejecutar la aplicacion

```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux/macOS
./mvnw spring-boot:run
```

La aplicacion inicia por defecto con H2 en memoria (`jdbc:h2:mem:javatest;MODE=PostgreSQL`) y aplica las migraciones Flyway automaticamente desde `src/main/resources/db/migration`.

### Ejecutar las pruebas

```bash
# Windows
.\mvnw.cmd test

# Linux/macOS
./mvnw test
```

Incluye 2 pruebas unitarias (`ReservaServiceTest`) y 1 prueba de integracion con `MockMvc` y contexto completo (`ReservaApiIntegrationTest`).

### Configuracion para PostgreSQL (opcional)

El proyecto usa H2 por defecto, pero incluye el driver de PostgreSQL. Para usar PostgreSQL, ajustar las propiedades de conexion en `src/main/resources/application.properties` (o mediante variables/propiedades externas):

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/javatest
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.username=tu_usuario
spring.datasource.password=tu_contrasena
```

Flyway ejecutara las migraciones al iniciar la aplicacion contra la base configurada.

## Autenticacion

Los endpoints de creacion y cancelacion requieren HTTP Basic:

- Usuario: `admin`
- Contrasena: `admin123`

Los endpoints `GET` de consulta no requieren autenticacion.

## Endpoints

### Salas

- `POST /salas` (requiere autenticacion)
  - Body:
    ```json
    { "nombre": "Sala A" }
    ```
- `GET /salas` (publico)

### Reservas

- `POST /reservas` (requiere autenticacion)
  - Body:
    ```json
    {
      "salaId": 1,
      "responsable": "Ana Perez",
      "inicio": "2026-11-24T10:00:00",
      "fin": "2026-11-24T11:00:00"
    }
    ```
  - Respuestas relevantes:
    - `201 Created`: reserva creada
    - `400 Bad Request`: datos invalidos (campos obligatorios, formato o rango de fechas). La respuesta incluye `detalles` con el campo y mensaje.
    - `404 Not Found`: sala inexistente
    - `409 Conflict`: sala ocupada en ese horario

- `GET /reservas` (publico, paginado)
  - Parametros opcionales:
    - `salaId`: filtra por sala
    - `fecha`: filtra reservas que se solapan con la fecha (`YYYY-MM-DD`)
    - `page`, `size`, `sort`: parametros de paginacion de Spring
  - Ejemplo: `/reservas?salaId=1&fecha=2026-11-24&page=0&size=10`

- `DELETE /reservas/{id}` (requiere autenticacion)
  - Cancela la reserva indicada.
  - Respuesta: `204 No Content`

## Decisiones que tome

- **Stack y build**: Spring Boot 3.3.5 con Java 17 y Maven Wrapper para que el proyecto sea reproducible sin depender de una instalacion global de Maven.
- **Persistencia y migraciones**: H2 en memoria con modo PostgreSQL por defecto para facilitar la ejecucion local y de pruebas sin infraestructura externa; driver de PostgreSQL incluido para apuntar a una base PostgreSQL real cambiando la configuracion. Esquema versionado con Flyway (`V1__create_rooms_and_reservations.sql`) y `spring.jpa.hibernate.ddl-auto=validate` para asegurar que el modelo JPA coincida con la migracion.
- **Modelo de dominio**: entidades `Sala` y `Reserva` (con estado `ACTIVA`/`CANCELADA`, sala, responsable, inicio y fin) y restriccion de rango en base de datos (`fin > inicio`). La cancelacion marca la reserva como `CANCELADA` (borrado logico) en lugar de eliminarla fisicamente.
- **Deteccion de solapamientos y conflictos**: se considera ocupada una sala cuando existe otra reserva `ACTIVA` con solapamiento de intervalo (`inicio < finNuevo AND fin > inicioNuevo`), devolviendo `409 Conflict`. La validacion de negocio adicional (por ejemplo, `fin` posterior a `inicio`) devuelve `400 Bad Request`.
- **Validacion y errores claros**: Bean Validation en los DTOs de entrada y un `GlobalExceptionHandler` que unifica respuestas `400` con una lista de `detalles` (campo y mensaje), ademas de manejo explicito de `404` y `409`.
- **Listado y paginacion**: `GET /reservas` usa `Pageable` de Spring Data y `JpaSpecificationExecutor` para filtrar opcionalmente por `salaId` y por `fecha` (reservas cuyo intervalo se solape con el dia indicado).
- **Seguridad**: HTTP Basic con usuario en memoria configurable (`app.security.username`/`app.security.password`) y CSRF deshabilitado al tratarse de una API; los endpoints `GET` de consulta son publicos y los de creacion/cancelacion (`POST /salas`, `POST /reservas`, `DELETE /reservas/**`) requieren autenticacion.
- **Pruebas**: pruebas unitarias del servicio de reservas para reglas de validacion/conflicto y una prueba de integracion con `@SpringBootTest` + `MockMvc` que cubre seguridad (`401` sin credenciales), validaciones (`400`), conflicto por solapamiento (`409`), creacion/listado con filtros y paginacion, y cancelacion autenticada.

## Que haria con mas tiempo

- **Pruebas con PostgreSQL real**: agregar pruebas de integracion con Testcontainers contra PostgreSQL para validar compatibilidad real de migraciones y consultas, ademas de ampliar casos (bordes de solapamiento, concurrencia y condiciones de carrera).
- **Concurrencia y consistencia**: reforzar la prevencion de solapamientos concurrentes con mecanismos a nivel de base de datos (por ejemplo, bloqueos, constraints/exclusion constraints en PostgreSQL o serializacion de operaciones por sala).
- **Seguridad**: migrar a JWT u OAuth2, manejo de roles, politicas de contrasenas/secretos externos y configuracion diferenciada por entorno.
- **API y documentacion**: publicar especificacion OpenAPI/Swagger, ejemplos adicionales, codigos de error mas detallados y paginacion/orden estables documentados.
- **Operabilidad**: perfiles por entorno (dev/test/prod), Docker/Docker Compose, observabilidad (logs estructurados, metricas y trazas) y pipeline de CI.
- **Dominio**: manejo explicito de zonas horarias, reglas de negocio adicionales (horarios permitidos, duracion minima/maxima, reprogramacion) e idempotencia en operaciones sensibles.

## Como use la IA

### Que le pedi

- Generar el codigo para cumplir los requisitos 1 a 6 del proyecto de pruebas (salas y reservas con sus reglas, validaciones, filtros/paginacion, base de datos con migraciones, seguridad y pruebas), con la condicion primordial de realizar commits separados y no todo de golpe.
- Revisar el ultimo requisito (7) y actualizar el `README.md` para que incluyera como ejecutarlo, las decisiones tomadas, que haria con mas tiempo y como use la IA (que le pedi, que acepte y que corregi).

### Que acepte

- La propuesta de estructura del proyecto Spring Boot (Maven Wrapper, configuracion base, capas de controlador/servicio/repositorio/DTO) y la division del trabajo en commits separados por etapas (bootstrap, dominio/migracion, API, seguridad, pruebas y documentacion).
- La implementacion alineada con los requisitos: migracion Flyway y entidades JPA, endpoints REST, validaciones con respuestas de error claras, deteccion de solapamiento con respuesta `409`, listado paginado con filtros, autenticacion HTTP Basic para operaciones de creacion/cancelacion y pruebas unitarias e integracion con `MockMvc`.
- La redaccion base de la documentacion de ejecucion, autenticacion, endpoints y configuracion de base de datos incorporada al README.

### Que corregi

- **Codificacion de archivos en Windows**: algunos archivos generados inicialmente con BOM UTF-8 provocaron errores de compilacion (`illegal character: '\ufeff'`); se reescribieron sin BOM y se recompilo el proyecto correctamente.
- **Organizacion de commits**: se ajusto el staging (incluyendo un amend del commit de bootstrap) para mantener cambios y commits atomicos/separados segun lo solicitado, evitando mezclar correcciones de codificacion o funcionalidades de distintas etapas.
- **Verificacion y ajustes de salida**: se ejecuto la compilacion y la suite de pruebas (`mvnw.cmd -DskipTests compile` y `mvnw.cmd test`), se revisaron los reportes de Surefire cuando la salida de consola fue verbosa, y se confirmo que las pruebas cubrieran los escenarios requeridos (validacion `400`, conflicto `409`, seguridad en mutaciones y listado con filtros/paginacion) antes de dar por finalizado cada commit.
