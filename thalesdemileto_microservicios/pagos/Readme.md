# 💳 Microservicio de Pagos

Este microservicio se encarga de gestionar las operaciones relacionadas con los **pagos dentro del sistema**, como registro de pagos, consultas, estados de pago u otros procesos asociados.

Está desarrollado con **Spring Boot**, utiliza **PostgreSQL** como base de datos (mediante **Spring Data JPA / Hibernate**), se comunica con el microservicio de **Login** para validaciones y puede utilizar **Apache Kafka** para el envío de eventos entre microservicios.

---

## 🏦 ¿Por qué PostgreSQL?

Las transacciones financieras requieren propiedades **ACID** (atomicidad, consistencia, aislamiento y durabilidad), que las bases de datos relacionales manejan de forma nativa. Por eso este microservicio migró de MongoDB a PostgreSQL.

En la práctica esto significa:

- **Atomicidad**: al confirmar o reembolsar un pago, la actualización del pago y de la deuda se guardan juntas o no se guarda ninguna.
- **Consistencia**: los montos se guardan como `NUMERIC(15,2)` (sin errores de redondeo) y la base de datos impide duplicar referencias externas, intentos de Stripe o números de comprobante (`UNIQUE`).
- **Aislamiento**: las operaciones que modifican saldos bloquean la fila correspondiente (`SELECT ... FOR UPDATE`), evitando que dos pagos simultáneos descuenten el mismo saldo pendiente.

Más detalles en la sección [🔒 Transacciones y concurrencia](#-transacciones-y-concurrencia).

---

## 🚀 Configuración inicial

El archivo principal de configuración se encuentra en:

```text
src/main/resources/application.properties
```

> ⚠️ Este archivo está incluido en `.gitignore`, por lo que cada desarrollador debe crearlo en su equipo.

La configuración base es:

```properties
spring.application.name=pagos
server.port=8084

servicio.login.url=

# PostgreSQL
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/pagos_db}
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD:postgres}
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=${DDL_AUTO:update}
spring.jpa.open-in-view=false
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.jdbc.time_zone=UTC

spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer

stripe.secret-key=sk_test_xxxxxxxxx
```

> ⚠️ La propiedad `stripe.secret-key` es **obligatoria**: si no está definida, el microservicio no inicia (`Could not resolve placeholder 'stripe.secret-key'`).

Para evitar colocar URLs y credenciales directamente en el proyecto, se recomienda utilizar **variables de entorno**.

---

# ⚙️ Configuración recomendada

Puedes dejar tu `application.properties` de esta manera:

```properties
# ==========================================
# MICROSERVICIO DE PAGOS
# ==========================================

spring.application.name=pagos

server.port=${PORT:8084}


# ==========================================
# MICROSERVICIO DE LOGIN
# ==========================================

servicio.login.url=${LOGIN_SERVICE_URL}


# ==========================================
# POSTGRESQL
# ==========================================

spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/pagos_db}
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


# ==========================================
# STRIPE
# ==========================================

stripe.secret-key=${STRIPE_SECRET_KEY}
```

---

# 🔐 Variables de entorno

Para ejecutar correctamente el microservicio necesitas configurar principalmente estas variables:

```text
LOGIN_SERVICE_URL
DB_URL
DB_USERNAME
DB_PASSWORD
KAFKA_BOOTSTRAP_SERVERS
STRIPE_SECRET_KEY
```

Opcionalmente:

```text
DDL_AUTO   (por defecto: update)
PORT       (por defecto: 8084)
```

---

## 🔑 `LOGIN_SERVICE_URL`

Esta variable contiene la dirección del microservicio de Login.

Para trabajar localmente:

```text
LOGIN_SERVICE_URL=http://localhost:8081
```

Si Login se encuentra desplegado en Render:

```text
LOGIN_SERVICE_URL=https://login-microservice-gfy0.onrender.com/
```

Spring utilizará este valor mediante:

```properties
servicio.login.url=${LOGIN_SERVICE_URL}
```

De esta manera no necesitas cambiar la URL dentro del código Java cuando pases de desarrollo a producción.

---

## 🗄️ `DB_URL`, `DB_USERNAME` y `DB_PASSWORD`

Estas variables contienen la conexión hacia PostgreSQL.

`DB_URL` usa el formato JDBC:

```text
jdbc:postgresql://HOST:PUERTO/BASE_DE_DATOS
```

Para trabajar con PostgreSQL local:

```text
DB_URL=jdbc:postgresql://localhost:5432/pagos_db
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

> 🔒 Se recomienda no subir usuarios, contraseñas o cadenas completas de conexión a GitHub.

---

## 🧱 `DDL_AUTO`

Controla cómo Hibernate gestiona el esquema de la base de datos:

| Valor | Comportamiento |
|---|---|
| `update` | (por defecto) Crea las tablas y columnas que falten, sin borrar datos. |
| `validate` | No modifica nada; solo verifica que el esquema coincida con las entidades. Recomendado en producción si el esquema se gestiona aparte. |
| `create` | Borra y vuelve a crear las tablas en cada inicio. ⚠️ **Se pierden los datos.** |
| `none` | No hace nada con el esquema. |

---

## 📨 `KAFKA_BOOTSTRAP_SERVERS`

Esta variable indica dónde se encuentra Apache Kafka.

En desarrollo local normalmente será:

```text
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

La configuración:

```properties
spring.kafka.bootstrap-servers=${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
```

significa que si la variable no está definida, Spring utilizará automáticamente:

```text
localhost:9092
```

---

## 💳 `STRIPE_SECRET_KEY`

Clave secreta de Stripe utilizada para crear los `PaymentIntent`.

En desarrollo utiliza una clave de prueba:

```text
STRIPE_SECRET_KEY=sk_test_xxxxxxxxx
```

> 🔒 Nunca subas la clave secreta de Stripe a GitHub.

---

# 💻 Configurar variables en Windows

Si utilizas **CMD**, puedes configurarlas temporalmente de esta manera:

```cmd
set LOGIN_SERVICE_URL=http://localhost:8081
set DB_URL=jdbc:postgresql://localhost:5432/pagos_db
set DB_USERNAME=postgres
set DB_PASSWORD=postgres
set KAFKA_BOOTSTRAP_SERVERS=localhost:9092
set STRIPE_SECRET_KEY=sk_test_xxxxxxxxx
```

Después ejecuta el microservicio desde esa misma consola.

---

Si utilizas **PowerShell**:

```powershell
$env:LOGIN_SERVICE_URL="http://localhost:8081"
$env:DB_URL="jdbc:postgresql://localhost:5432/pagos_db"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="postgres"
$env:KAFKA_BOOTSTRAP_SERVERS="localhost:9092"
$env:STRIPE_SECRET_KEY="sk_test_xxxxxxxxx"
```

---

# 🐘 Preparar PostgreSQL local

La base de datos debe existir antes de iniciar el microservicio (Hibernate crea las **tablas**, pero no la **base de datos**).

Con `psql`:

```sql
CREATE DATABASE pagos_db;
```

O, si prefieres Docker:

```cmd
docker run -d --name postgres-pagos -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=pagos_db -p 5432:5432 postgres:16
```

Si la base no existe, al iniciar aparecerá un error como:

```text
FATAL: no existe la base de datos «pagos_db»
```

---

# 🧪 Configuración completamente local

Para realizar pruebas rápidas en tu computadora puedes utilizar directamente:

```properties
spring.application.name=pagos

server.port=8084

servicio.login.url=http://localhost:8081

spring.datasource.url=jdbc:postgresql://localhost:5432/pagos_db
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.open-in-view=false
spring.jpa.properties.hibernate.jdbc.time_zone=UTC

spring.kafka.bootstrap-servers=localhost:9092

spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer

spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer

stripe.secret-key=sk_test_xxxxxxxxx
```

Con esta configuración debes tener funcionando:

```text
Login       → http://localhost:8081
Pagos       → http://localhost:8084
PostgreSQL  → localhost:5432
Kafka       → localhost:9092
```

---

# 🌐 Configuración para producción

Para producción es mejor mantener todo mediante variables de entorno:

```properties
spring.application.name=pagos

server.port=${PORT:8084}

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

stripe.secret-key=${STRIPE_SECRET_KEY}
```

En el servidor podrías tener variables como:

```text
LOGIN_SERVICE_URL=https://login-microservice-gfy0.onrender.com/

DB_URL=jdbc:postgresql://host-remoto:5432/pagos_db?sslmode=require
DB_USERNAME=usuario
DB_PASSWORD=password

KAFKA_BOOTSTRAP_SERVERS=servidor-kafka:9092

STRIPE_SECRET_KEY=sk_live_xxxxxxxxx
```

> 💡 En el primer despliegue puedes usar `DDL_AUTO=update` para que se creen las tablas, y luego cambiar a `validate`.

---

# ▶️ Ejecutar el microservicio

Desde la carpeta raíz del proyecto puedes ejecutar:

```cmd
.\mvnw.cmd spring-boot:run
```

---

Para compilar el proyecto sin ejecutar las pruebas:

```cmd
.\mvnw.cmd clean package -DskipTests
```

El archivo generado normalmente se encontrará en:

```text
target/
```

Después puedes ejecutarlo con:

```cmd
java -jar target/pagos-1.0.0.jar
```

El nombre exacto del `.jar` puede variar dependiendo de la configuración del `pom.xml`.

---

# 🔎 Verificar que está funcionando

El microservicio de Pagos utiliza por defecto el puerto:

```text
8084
```

Por lo tanto, su URL base en desarrollo será:

```text
http://localhost:8084
```

Si en la consola aparece algo parecido a:

```text
Tomcat started on port 8084
Started PagosApplication in ... seconds
>>> Ejecutando Seeder pagos...
>>> Seeder pagos completado correctamente
```

significa que el microservicio inició correctamente, creó las tablas y cargó los datos de prueba.

El seeder solo se ejecuta si la tabla `deudas` está vacía; en los siguientes inicios mostrará:

```text
>>> Seeder pagos omitido: ya existen datos
```

---

# 🔗 Comunicación con Login

El microservicio de Pagos puede comunicarse con Login para validar usuarios o tokens.

El flujo puede verse así:

```text
Frontend
   │
   ▼
Pagos
http://localhost:8084
   │
   │ Validación de usuario/token
   ▼
Login
http://localhost:8081
```

La URL del Login se obtiene desde:

```properties
servicio.login.url=${LOGIN_SERVICE_URL}
```

Por eso no es necesario colocarla directamente dentro de los controladores o servicios Java.

---

# 🐘 PostgreSQL

La conexión se realiza mediante:

```properties
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/pagos_db}
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD:postgres}
```

## Tablas

Hibernate genera automáticamente las siguientes tablas (los nombres de columna se convierten a `snake_case`, por ejemplo `deudaId` → `deuda_id`):

| Entidad | Tabla | Restricciones destacadas |
|---|---|---|
| `Deuda` | `deudas` | — |
| `Pago` | `pagos` | `referencia_externa` única |
| `IntentoPago` | `intentos_pago` | `payment_intent_id` única |
| `Comprobante` | `comprobantes` | `numero_comprobante` única |
| `PasarelaPago` | `pasarelas_pago` | — |

Características comunes:

- Los `id` son de tipo `String` y se generan automáticamente como **UUID** (columna `VARCHAR(36)`). Los endpoints siguen recibiendo y devolviendo el `id` como texto, igual que antes.
- Los montos (`monto`, `saldoPendiente`) se guardan como `NUMERIC(15,2)`.
- Las fechas (`LocalDateTime`) se guardan como `TIMESTAMP` en UTC.
- Los campos de cada entidad son los mismos que existían con MongoDB.

## Errores frecuentes

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
FATAL: no existe la base de datos «pagos_db»
```

revisa lo siguiente:

- PostgreSQL debe estar iniciado si trabajas localmente.
- La base de datos `pagos_db` debe existir (ver [Preparar PostgreSQL local](#-preparar-postgresql-local)).
- `DB_URL` debe tener el formato `jdbc:postgresql://...`.
- Usuario y contraseña deben ser correctos.
- La base de datos remota debe permitir conexiones desde tu IP y, normalmente, requerir `?sslmode=require`.
- Si el microservicio está desplegado en Render, no debe apuntar a `localhost:5432` salvo que PostgreSQL también se encuentre allí.

---

# 🔒 Transacciones y concurrencia

## Servicios transaccionales

Todos los servicios (`PagoService`, `DeudaService`, `IntentoPagoService`, `ComprobanteService`, `PasarelaPagoService`) están anotados con `@Transactional`. Cada método público se ejecuta dentro de una transacción: si ocurre una excepción, **todos** los cambios de ese método se revierten.

Esto es especialmente importante en `PagoService`:

| Operación | Cambios que se aplican de forma atómica |
|---|---|
| `confirmarPago` | Descuenta el saldo de la deuda, cambia su estado (`PARCIAL` / `PAGADA`) y marca el pago como `CONFIRMADO`. |
| `reembolsarPago` | Restituye el saldo de la deuda, cambia su estado (`PENDIENTE` / `PARCIAL`) y marca el pago como `REEMBOLSADO`. |

El `DataSeeder` también es transaccional: si falla a mitad de la carga, no quedan datos de prueba incompletos.

## Bloqueo de filas

Para evitar condiciones de carrera (por ejemplo, dos confirmaciones simultáneas del mismo pago, o dos pagos sobre la misma deuda), los repositorios exponen consultas con bloqueo pesimista:

```java
// DeudaRepository
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT d FROM Deuda d WHERE d.id = :id")
Optional<Deuda> findByIdForUpdate(@Param("id") String id);

// PagoRepository
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT p FROM Pago p WHERE p.id = :id")
Optional<Pago> findByIdForUpdate(@Param("id") String id);
```

En PostgreSQL esto se traduce en `SELECT ... FOR UPDATE`: la fila queda bloqueada hasta que termina la transacción, y cualquier otra transacción que intente modificarla espera su turno.

`PagoService` utiliza estos métodos en `registrar`, `confirmarPago`, `rechazarPago` y `reembolsarPago`.

> 💡 Si agregas nuevas operaciones que modifiquen saldos o estados de pago, utiliza `findByIdForUpdate` y mantenlas dentro de un método `@Transactional`.

## Flujo con Stripe

En `StripeController`, el pago se registra en la base de datos **antes** de crear el `PaymentIntent` en Stripe, y luego se actualiza con la referencia externa. La llamada a Stripe queda fuera de la transacción a propósito: no conviene mantener filas bloqueadas mientras se espera la respuesta de un servicio externo.

---

# 🔄 Migración desde MongoDB

Este microservicio utilizaba MongoDB anteriormente. Los cambios realizados fueron:

- `spring-boot-starter-data-mongodb` → `spring-boot-starter-data-jpa` + driver `org.postgresql:postgresql`.
- `@Document(collection = "...")` → `@Entity` + `@Table(name = "...")`, manteniendo los mismos nombres.
- `MongoRepository` → `JpaRepository`. Las consultas derivadas (`findBy...`, `existsBy...`) no cambiaron.
- `spring.data.mongodb.uri` → `spring.datasource.*` + `spring.jpa.*`.

> ⚠️ Los datos existentes en MongoDB **no se migran automáticamente**. Si necesitas conservarlos, debes exportarlos (por ejemplo con `mongoexport`) e importarlos en PostgreSQL. Ten en cuenta que los `id` antiguos de MongoDB (`ObjectId`, 24 caracteres) caben en la columna `VARCHAR(36)`, por lo que pueden conservarse.

---

# 📨 Apache Kafka

Kafka permite enviar eventos desde el microservicio de Pagos hacia otros servicios.

La configuración del productor es:

```properties
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer

spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
```

Esto significa que:

- Las claves de los mensajes se envían como texto.
- Los objetos Java pueden convertirse automáticamente a JSON.

Kafka se obtiene desde:

```properties
spring.kafka.bootstrap-servers=${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
```

---

# 📌 Puertos principales

| Servicio | Puerto |
|---|---:|
| Login | `8081` |
| Matrícula | `8082` |
| Académico | `8083` |
| Pagos | `8084` |
| PostgreSQL | `5432` |
| Kafka | `9092` |

---

# 📁 Estructura general

```text
pagos/
│
├── src/
│   ├── main/
│   │   ├── java/com/thales_mileto/pagos/
│   │   │   ├── config/        → seguridad, filtro de login, DataSeeder
│   │   │   ├── controller/    → endpoints REST
│   │   │   ├── dto/           → objetos de request/response
│   │   │   ├── event/         → eventos de Kafka
│   │   │   ├── kafka/         → productor de Kafka
│   │   │   ├── model/         → entidades JPA
│   │   │   ├── repository/    → repositorios JpaRepository
│   │   │   └── service/       → lógica de negocio (transaccional)
│   │   │
│   │   └── resources/
│   │       └── application.properties
│   │
│   └── test/
│
├── Dockerfile
├── pom.xml
├── mvnw
├── mvnw.cmd
└── Readme.md
```

---

# ✅ `application.properties` final recomendado

Puedes dejarlo así:

```properties
# ==========================================
# MICROSERVICIO DE PAGOS
# ==========================================

spring.application.name=pagos

server.port=${PORT:8084}


# ==========================================
# LOGIN
# ==========================================

servicio.login.url=${LOGIN_SERVICE_URL}


# ==========================================
# POSTGRESQL
# ==========================================

spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/pagos_db}
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
# KAFKA
# ==========================================

spring.kafka.bootstrap-servers=${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}

spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer

spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer


# ==========================================
# STRIPE
# ==========================================

stripe.secret-key=${STRIPE_SECRET_KEY}
```

Con esta configuración, el microservicio **Pagos** queda preparado para trabajar tanto en **desarrollo local** como en **producción**, manteniendo separadas las URLs, credenciales y configuraciones sensibles.
