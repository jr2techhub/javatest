# javatest

Proyecto de pruebas en Spring Boot para la gestion de salas y reservas.

## Requisitos implementados

1. Crear y listar salas. Crear, listar y cancelar reservas. Cada reserva incluye sala, responsable, inicio y fin.
2. Si la sala ya esta ocupada en el horario solicitado, la API responde `409 Conflict`. Las validaciones de datos responden `400 Bad Request` con detalles claros.
3. Listado de reservas con filtros por sala y por fecha, con paginacion.
4. Base de datos H2 por defecto con migraciones Flyway. Tambien se incluye driver PostgreSQL para poder configurar una base PostgreSQL.
5. Los endpoints que crean o cancelan requieren autenticacion HTTP Basic; los endpoints de consulta son publicos.
6. Pruebas automatizadas: 2 unitarias y 1 de integracion con `MockMvc` y contexto completo de Spring Boot.

## Requisitos previos

- Java 17+
- Maven Wrapper incluido en el proyecto (`mvnw.cmd` en Windows)

## Ejecucion

```bash
# Windows
mvnw.cmd spring-boot:run

# Linux/macOS
./mvnw spring-boot:run
```

La aplicacion inicia por defecto con H2 en memoria y aplica las migraciones Flyway automaticamente.

## Pruebas

```bash
# Windows
mvnw.cmd test

# Linux/macOS
./mvnw test
```

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
    - `400 Bad Request`: datos invalidos (campos obligatorios, formato/rango de fechas)
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

## Base de datos y migraciones

- Configuracion por defecto: H2 en memoria (`jdbc:h2:mem:javatest`) con modo PostgreSQL.
- Migraciones: Flyway (`src/main/resources/db/migration`).
- Para usar PostgreSQL, ajustar `spring.datasource.*` en `application.properties` (o variables/propiedades externas) con la URL, usuario y contrasena del servidor PostgreSQL.
