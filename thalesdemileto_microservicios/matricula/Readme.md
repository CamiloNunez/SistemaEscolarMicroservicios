# 📚 Microservicio de Matrícula

Este microservicio se encarga de gestionar las operaciones relacionadas con la **matrícula de estudiantes** dentro del sistema.

El proyecto está desarrollado con **Spring Boot**, utiliza **PostgreSQL** como base de datos (mediante **Spring Data JPA / Hibernate**), se comunica con el microservicio de **Login** y puede utilizar **Apache Kafka** para el envío y consumo de eventos.

---

## 🏫 ¿Por qué PostgreSQL?

El control de vacantes exige **atomicidad**: registrar una matrícula y descontar el cupo de su sección deben ocurrir juntos o no ocurrir. Con MongoDB eran dos escrituras independientes en colecciones distintas, y dos matrículas simultáneas podían leer el mismo cupo libre y sobrevender la sección. Por eso este microservicio migró a PostgreSQL.

En la práctica esto significa:

- **Atomicidad**: la matrícula y el contador de su vacante se guardan en la misma transacción.
- **Aislamiento**: antes de tocar un cupo se bloquea la fila de la vacante (`SELECT ... FOR UPDATE`); las matrículas simultáneas sobre la misma sección se atienden una detrás de otra.
- **Consistencia**: un `CHECK` en la tabla `vacantes` impide en la propia base de datos que los cupos queden negativos o que `ocupadas + reservadas` supere el total.

Más detalles en la sección [🔒 Control de vacantes y transacciones](#-control-de-vacantes-y-transacciones).

---

## 🚀 Configuración inicial

Antes de ejecutar el proyecto, debes configurar las propiedades necesarias en:

```text
src/main/resources/application.properties
```

> ⚠️ Este archivo está incluido en `.gitignore`, por lo que cada desarrollador debe crearlo en su equipo.

Una configuración base puede quedar de la siguiente manera:

```properties
spring.application.name=matricula

server.port=8082

servicio.login.url=${LOGIN_SERVICE_URL}

# PostgreSQL
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/matricula_db}
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD:postgres}
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=${DDL_AUTO:update}
spring.jpa.open-in-view=false
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.jdbc.time_zone=UTC

spring.kafka.bootstrap-servers=${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}

spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
```

---

## ⚙️ Variables de entorno necesarias

El microservicio necesita principalmente las siguientes variables:

```text
LOGIN_SERVICE_URL
DB_URL
DB_USERNAME
DB_PASSWORD
KAFKA_BOOTSTRAP_SERVERS
```

Opcionalmente:

```text
DDL_AUTO   (por defecto: update)
PORT       (por defecto: 8082)
```

### `LOGIN_SERVICE_URL`

URL donde se encuentra ejecutándose el microservicio de Login.

Por ejemplo, en desarrollo local:

```text
LOGIN_SERVICE_URL=http://localhost:8081
```

Si el microservicio de Login se encuentra desplegado en Render:

```text
LOGIN_SERVICE_URL=https://login-microservice-gfy0.onrender.com/
```

De esta manera, en el código no necesitamos cambiar manualmente la URL dependiendo del entorno.

---

### `DB_URL`, `DB_USERNAME` y `DB_PASSWORD`

Estas variables contienen la conexión hacia PostgreSQL.

`DB_URL` usa el formato JDBC:

```text
jdbc:postgresql://HOST:PUERTO/BASE_DE_DATOS
```

Para trabajar con PostgreSQL local:

```text
DB_URL=jdbc:postgresql://localhost:5432/matricula_db
DB_USERNAME=postgres
DB_PASSWORD=postgres
```

Si no defines estas variables, Spring utilizará automáticamente esos mismos valores por defecto.

Si utilizas un PostgreSQL remoto (Render, Neon, Supabase, AWS RDS, etc.):

```text
DB_URL=jdbc:postgresql://HOST:5432/BASE_DE_DATOS?sslmode=require
DB_USERNAME=USUARIO
DB_PASSWORD=CONTRASEÑA
```

> 💡 Muchos proveedores entregan la conexión en formato `postgres://usuario:password@host:5432/db`. Spring **no** acepta ese formato directamente: debes convertirlo a `jdbc:postgresql://host:5432/db` y pasar usuario y contraseña en `DB_USERNAME` y `DB_PASSWORD`.

> 🔒 Se recomienda no guardar usuarios, contraseñas ni cadenas de conexión privadas directamente dentro de `application.properties`.

---

### `DDL_AUTO`

Controla cómo Hibernate gestiona el esquema de la base de datos:

| Valor | Comportamiento |
|---|---|
| `update` | (por defecto) Crea las tablas y columnas que falten, sin borrar datos. |
| `validate` | No modifica nada; solo verifica que el esquema coincida con las entidades. Recomendado en producción si el esquema se gestiona aparte. |
| `create` | Borra y vuelve a crear las tablas en cada inicio. ⚠️ **Se pierden los datos.** |
| `none` | No hace nada con el esquema. |

> ⚠️ Con `update`, Hibernate crea las restricciones (`UNIQUE`, `CHECK`) solo al crear una tabla nueva; no las agrega a tablas que ya existían.

---

### `KAFKA_BOOTSTRAP_SERVERS`

Dirección del servidor de Apache Kafka.

Para trabajar localmente:

```text
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

Esta variable es opcional porque en `application.properties` ya dejamos:

```properties
spring.kafka.bootstrap-servers=${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
```

Esto significa que, si no existe la variable `KAFKA_BOOTSTRAP_SERVERS`, Spring utilizará automáticamente:

```text
localhost:9092
```

---

## 🪟 Configurar variables en Windows

Desde CMD puedes configurarlas temporalmente con:

```cmd
set LOGIN_SERVICE_URL=http://localhost:8081
set DB_URL=jdbc:postgresql://localhost:5432/matricula_db
set DB_USERNAME=postgres
set DB_PASSWORD=postgres
set KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

Luego puedes ejecutar el proyecto desde esa misma consola.

En PowerShell:

```powershell
$env:LOGIN_SERVICE_URL="http://localhost:8081"
$env:DB_URL="jdbc:postgresql://localhost:5432/matricula_db"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="postgres"
$env:KAFKA_BOOTSTRAP_SERVERS="localhost:9092"
```

---

## 🐘 Preparar PostgreSQL local

La base de datos debe existir antes de iniciar el microservicio (Hibernate crea las **tablas**, pero no la **base de datos**).

Con `psql`:

```sql
CREATE DATABASE matricula_db;
```

O, si prefieres Docker:

```cmd
docker run -d --name postgres-matricula -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=matricula_db -p 5432:5432 postgres:16
```

Si la base no existe, al iniciar aparecerá un error como:

```text
FATAL: no existe la base de datos «matricula_db»
```

---

## 🧪 Configuración para desarrollo local

Para trabajar completamente en local puedes utilizar:

```properties
spring.application.name=matricula

server.port=8082

servicio.login.url=http://localhost:8081

spring.datasource.url=jdbc:postgresql://localhost:5432/matricula_db
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.open-in-view=false
spring.jpa.properties.hibernate.jdbc.time_zone=UTC

spring.kafka.bootstrap-servers=localhost:9092

spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
```

En este caso debes tener funcionando:

```text
Login       → http://localhost:8081
Matrícula   → http://localhost:8082
PostgreSQL  → localhost:5432
Kafka       → localhost:9092
```

---

## 🌐 Configuración recomendada para producción

Para producción es mejor utilizar variables de entorno:

```properties
spring.application.name=matricula

server.port=${PORT:8082}

servicio.login.url=${LOGIN_SERVICE_URL}

spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=${DDL_AUTO:validate}
spring.jpa.open-in-view=false
spring.jpa.properties.hibernate.jdbc.time_zone=UTC

spring.kafka.bootstrap-servers=${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}

spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
```

Esto permite desplegar el mismo código en distintos servidores sin modificar el proyecto.

Por ejemplo:

```text
LOGIN_SERVICE_URL=https://login-microservice-gfy0.onrender.com/

DB_URL=jdbc:postgresql://host-remoto:5432/matricula_db?sslmode=require
DB_USERNAME=usuario
DB_PASSWORD=password

KAFKA_BOOTSTRAP_SERVERS=servidor-kafka:9092
```

> 💡 En el primer despliegue puedes usar `DDL_AUTO=update` para que se creen las tablas, y luego cambiar a `validate`.

---

## ▶️ Ejecutar el proyecto

Desde la carpeta raíz del microservicio:

### Windows

```cmd
.\mvnw.cmd spring-boot:run
```

También puedes generar el proyecto con Maven:

```cmd
.\mvnw.cmd clean package -DskipTests
```

Después puedes ejecutar el `.jar` generado:

```cmd
java -jar target/matricula-1.0.0.jar
```

---

## 🔎 Verificar que está funcionando

Por defecto, Matrícula utiliza el puerto `8082`, por lo que la dirección base será:

```text
http://localhost:8082
```

Si al iniciar aparece algo similar a:

```text
Tomcat started on port 8082
>>> Ejecutando Seeder inicial...
>>> Seeder completado correctamente: 13 tablas pobladas con al menos 3 registros cada una.
```

significa que Spring Boot inició correctamente, creó las tablas y cargó los datos de prueba.

El seeder solo se ejecuta si la tabla `programas_academicos` está vacía; en los siguientes inicios mostrará:

```text
>>> Seeder omitido: ya existen datos
```

---

## 📡 Comunicación con Login

Matrícula utiliza:

```properties
servicio.login.url=${LOGIN_SERVICE_URL}
```

para conocer la dirección del microservicio encargado de autenticación.

```text
Matrícula
   │
   │ valida usuario/token
   ▼
Login
```

En desarrollo:

```text
Matrícula → http://localhost:8082
Login     → http://localhost:8081
```

En producción, Matrícula puede continuar ejecutándose en otro servidor mientras Login se encuentra publicado en Render:

```text
Matrícula
    │
    ▼
https://login-microservice-gfy0.onrender.com/
```

---

## 📨 Apache Kafka

Kafka se utiliza para la comunicación basada en eventos entre microservicios.

La configuración actual del productor es:

```properties
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer

spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
```

Esto permite enviar:

- claves como texto (`String`);
- objetos Java convertidos automáticamente a JSON.

Kafka debe estar disponible en `localhost:9092` o en la dirección indicada mediante `KAFKA_BOOTSTRAP_SERVERS`.

---

## 🐘 PostgreSQL

### Tablas

Hibernate genera automáticamente las siguientes tablas (los nombres de columna se convierten a `snake_case`, por ejemplo `seccionId` → `seccion_id`):

| Entidad | Tabla | Restricciones destacadas |
|---|---|---|
| `ProgramaAcademico` | `programas_academicos` | `codigo` único |
| `Grado` | `grados` | (`programa_academico_id`, `numero`) único |
| `Curso` | `cursos` | `codigo` único |
| `PeriodoAcademico` | `periodos_academicos` | `codigo` único |
| `PlanEstudios` | `planes_estudio` + `plan_estudios_cursos` | (`programa_academico_id`, `grado_id`, `periodo_academico_id`) único |
| `Seccion` | `secciones` | (`grado_id`, `letra`, `periodo_academico_id`) único |
| `Vacante` | `vacantes` | (`seccion_id`, `periodo_academico_id`) único + `CHECK ck_vacante_cupos` |
| `Estudiante` | `estudiantes` + `estudiante_apoderados` + `estudiante_condiciones_especiales` | `dni` único, referencia de migración única |
| `Apoderado` | `apoderados` | `dni` único, `usuario_id` único, referencia de migración única |
| `Matricula` | `matriculas` | referencia de migración única |
| `DetalleMatricula` | `detalles_matricula` | (`matricula_id`, `curso_id`) único |
| `DocumentoMatricula` | `documentos_matricula` | — |
| `PagoMatricula` | `pagos_matricula` | `numero_comprobante` único |

Características comunes:

- Los `id` son de tipo `String` y se generan automáticamente como **UUID** (columna `VARCHAR(36)`). Los endpoints siguen recibiendo y devolviendo el `id` como texto, igual que antes.
- Los objetos embebidos (`direccion`, `referenciaMigracion`) se guardan como columnas de la propia tabla; `referenciaMigracion.datosOriginales` se guarda como `jsonb`.
- Las listas (`condicionesEspeciales`, `apoderadoIds`, `cursos` del plan) se guardan en tablas auxiliares.
- Los montos se guardan como `NUMERIC(15,2)` y las fechas en UTC.
- Las columnas `UNIQUE` admiten varios `NULL` (equivalente al índice *sparse* de MongoDB): por ejemplo, varios estudiantes sin DNI.

### Errores frecuentes

Si aparece un error como:

```text
Connection to localhost:5432 refused
```

o:

```text
FATAL: password authentication failed for user "postgres"
```

o:

```text
FATAL: no existe la base de datos «matricula_db»
```

revisa lo siguiente:

- PostgreSQL debe estar iniciado si trabajas localmente.
- La base de datos `matricula_db` debe existir (ver [Preparar PostgreSQL local](#-preparar-postgresql-local)).
- `DB_URL` debe tener el formato `jdbc:postgresql://...`.
- Usuario y contraseña deben ser correctos.
- La base de datos remota debe permitir conexiones desde tu IP y, normalmente, requerir `?sslmode=require`.
- No estés apuntando accidentalmente a `localhost` desde un servidor remoto.

---

## 🔒 Control de vacantes y transacciones

### Cómo se vincula una matrícula con su vacante

`Matricula` tiene dos campos nuevos, opcionales:

| Campo | Descripción |
|---|---|
| `seccionId` | `id` de la sección |
| `periodoAcademicoId` | `id` del periodo académico |

Con ambos se identifica la `Vacante` (única por sección y periodo). Deben enviarse **juntos o ninguno**. Si no se envían (por ejemplo, matrículas históricas migradas), la matrícula no consume cupo.

```json
POST /api/matriculas
{
  "estudianteId": "EST004",
  "nombreEstudiante": "Ana Torres",
  "grado": "Primer Grado",
  "periodo": "2026",
  "seccionId": "<id de la sección>",
  "periodoAcademicoId": "<id del periodo>"
}
```

Si `seccion` viene vacío, se completa con el `nombreSeccion` de la vacante.

### Cupo según el estado de la matrícula

| Estado de la matrícula | Cupo que consume |
|---|---|
| `REGISTRADA`, `PENDIENTE` (o cualquier estado en proceso) | `vacantesReservadas` |
| `CONFIRMADA` | `vacantesOcupadas` |
| `ANULADA`, `CANCELADA`, `RECHAZADA`, `RETIRADA` | ninguno (se libera) |

Operaciones y su efecto, todas atómicas:

| Operación | Efecto en la vacante |
|---|---|
| Crear matrícula | Reserva (u ocupa) un cupo. Si no hay cupo libre o la vacante está `CERRADA` → **409** y no se guarda la matrícula. |
| `PUT` con `estado: CONFIRMADA` | Pasa el cupo de reservado a ocupado (no requiere cupo libre: ya lo tenía). |
| `PUT` con estado de anulación | Libera el cupo. |
| `PUT` con otra `seccionId` | Libera el cupo en la sección anterior y lo toma en la nueva (409 si la nueva está llena). |
| `DELETE` matrícula | Libera el cupo. |

`vacantesDisponibles` (= total − ocupadas − reservadas) se calcula al vuelo y se devuelve en el JSON de `/api/vacantes`. El estado de la vacante pasa a `LLENA` / `DISPONIBLE` automáticamente; `CERRADA` solo se asigna explícitamente.

### Reglas en `/api/vacantes`

- `vacantesOcupadas` y `vacantesReservadas` **ya no se modifican por `PUT`**: los mantiene el servicio de matrículas. Si vienen en el cuerpo, se ignoran.
- `totalVacantes` no puede ser menor que los cupos en uso.
- No se puede cambiar la sección/periodo ni eliminar una vacante que tenga cupos en uso (**409**).

### Bloqueo de filas

```java
// VacanteRepository
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT v FROM Vacante v WHERE v.seccionId = :seccionId AND v.periodoAcademicoId = :periodoAcademicoId")
Optional<Vacante> findBySeccionIdAndPeriodoAcademicoIdForUpdate(...);

// MatriculaRepository
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT m FROM Matricula m WHERE m.id = :id")
Optional<Matricula> findByIdForUpdate(@Param("id") String id);
```

En PostgreSQL esto se traduce en `SELECT ... FOR UPDATE`: la fila queda bloqueada hasta que termina la transacción, y cualquier otra transacción que intente modificarla espera su turno. Al trasladar una matrícula entre secciones, las dos vacantes se bloquean siempre en el mismo orden para evitar *deadlocks*.

Como última defensa, la tabla tiene:

```sql
CONSTRAINT ck_vacante_cupos CHECK (
  vacantes_ocupadas >= 0 AND vacantes_reservadas >= 0 AND total_vacantes >= 0
  AND vacantes_ocupadas + vacantes_reservadas <= total_vacantes)
```

> 💡 Si agregas nuevas operaciones que cambien cupos, utiliza `VacanteService.bloquear(...)` y `VacanteService.moverCupo(...)` dentro de un método `@Transactional`.

### Servicios transaccionales

Todos los servicios están anotados con `@Transactional`: si ocurre una excepción, **todos** los cambios de ese método se revierten. Excepciones:

- `MatriculaService.migrar` guarda **cada fila en su propia transacción**: un error en una matrícula (por ejemplo, sección sin cupo) no revierte las demás del lote.
- `ApoderadoService.verificarVinculos` no abre transacción, para no retener una conexión de BD mientras llama a Login.

El `DataSeeder` también es transaccional: si falla a mitad de la carga, no quedan datos de prueba incompletos.

### Errores HTTP

| Situación | Código |
|---|---|
| Datos inválidos (`IllegalArgumentException`), p. ej. `seccionId` sin `periodoAcademicoId` o vacante inexistente | 400 |
| Sin cupo, vacante cerrada, duplicado de migración (`IllegalStateException`) | 409 |
| Violación de `UNIQUE` o `CHECK` en PostgreSQL | 409 |
| Fila bloqueada por otra operación y no se pudo esperar | 409 |

---

## 🔄 Migración desde MongoDB

Este microservicio utilizaba MongoDB anteriormente. Los cambios realizados fueron:

- `spring-boot-starter-data-mongodb` → `spring-boot-starter-data-jpa` + driver `org.postgresql:postgresql`.
- `@Document(collection = "...")` → `@Entity` + `@Table(name = "...")`, manteniendo los mismos nombres.
- `@Indexed` / `@CompoundIndex` y `MatriculaMongoInitializer` → `@UniqueConstraint` / `@Index` en cada entidad.
- `MongoRepository` → `JpaRepository`. Las consultas derivadas (`findBy...`, `existsBy...`) no cambiaron; las `@Query` de Mongo se reescribieron en JPQL.
- `spring.data.mongodb.uri` → `spring.datasource.*` + `spring.jpa.*`.

> ⚠️ Los datos existentes en MongoDB **no se migran automáticamente**. Si necesitas conservarlos, expórtalos (por ejemplo con `mongoexport`) e impórtalos en PostgreSQL. Los `id` antiguos (`ObjectId`, 24 caracteres) caben en `VARCHAR(36)`. Las matrículas antiguas no tienen `seccionId`/`periodoAcademicoId`, así que no consumen cupo; si las vinculas, ajusta los contadores de `vacantes` para que coincidan.

---

## 📌 Resumen de puertos

| Servicio | Puerto |
|---|---:|
| Login | `8081` |
| Matrícula | `8082` |
| PostgreSQL | `5432` |
| Kafka | `9092` |

---

## ✅ `application.properties` recomendado

La configuración final recomendada es:

```properties
# ==========================================
# MICROSERVICIO DE MATRÍCULA
# ==========================================

spring.application.name=matricula

# Puerto del microservicio
server.port=${PORT:8082}


# ==========================================
# MICROSERVICIO DE LOGIN
# ==========================================

servicio.login.url=${LOGIN_SERVICE_URL}


# ==========================================
# POSTGRESQL
# ==========================================

spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/matricula_db}
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD:postgres}
spring.datasource.driver-class-name=org.postgresql.Driver


# ==========================================
# JPA / HIBERNATE
# ==========================================

spring.jpa.hibernate.ddl-auto=${DDL_AUTO:update}
spring.jpa.open-in-view=false
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.jdbc.time_zone=UTC


# ==========================================
# APACHE KAFKA
# ==========================================

spring.kafka.bootstrap-servers=${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}

spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
```

Con esta configuración, el microservicio queda preparado tanto para **desarrollo local** como para **despliegue en producción**, evitando tener URLs, credenciales o configuraciones sensibles escritas directamente en el código.
