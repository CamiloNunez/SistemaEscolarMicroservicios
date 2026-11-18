# 🔔 Microservicio de Notificaciones

Este microservicio se encarga de gestionar las **notificaciones del sistema**, recibiendo eventos generados por otros microservicios y procesándolos para informar a los usuarios cuando corresponda.

Está desarrollado con **Spring Boot**, utiliza **MongoDB** para almacenar información relacionada con las notificaciones y se comunica con otros servicios mediante **Apache Kafka**.

A diferencia de otros microservicios del proyecto, este servicio está configurado principalmente como **consumidor de mensajes Kafka**.

---

## 🚀 Configuración inicial

El archivo principal de configuración se encuentra en:

```text
src/main/resources/application.properties
```

La configuración base es:

```properties
spring.application.name=notificaciones
server.port=8086

servicio.login.url=
spring.data.mongodb.uri=

spring.kafka.bootstrap-servers=localhost:9092

spring.kafka.consumer.group-id=notificaciones-service
spring.kafka.consumer.auto-offset-reset=earliest

spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer
```

Para evitar colocar URLs, credenciales o direcciones directamente dentro del proyecto, se recomienda utilizar **variables de entorno**.

---

# ⚙️ Configuración recomendada

Puedes dejar tu `application.properties` de la siguiente manera:

```properties
# ==========================================
# MICROSERVICIO DE NOTIFICACIONES
# ==========================================

spring.application.name=notificaciones

server.port=${PORT:8086}


# ==========================================
# MICROSERVICIO DE LOGIN
# ==========================================

servicio.login.url=${LOGIN_SERVICE_URL}


# ==========================================
# MONGODB
# ==========================================

spring.data.mongodb.uri=${MONGODB_URI}


# ==========================================
# APACHE KAFKA
# ==========================================

spring.kafka.bootstrap-servers=${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}


# ==========================================
# CONSUMIDOR KAFKA
# ==========================================

spring.kafka.consumer.group-id=notificaciones-service

spring.kafka.consumer.auto-offset-reset=earliest

spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer

spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer
```

---

# 🔐 Variables de entorno

Las principales variables necesarias son:

```text
LOGIN_SERVICE_URL
MONGODB_URI
KAFKA_BOOTSTRAP_SERVERS
```

---

## 🔑 `LOGIN_SERVICE_URL`

Esta variable indica dónde se encuentra ejecutándose el microservicio de Login.

En desarrollo local:

```text
LOGIN_SERVICE_URL=http://localhost:8081
```

Si Login está desplegado en Render:

```text
LOGIN_SERVICE_URL=https://login-microservice-gfy0.onrender.com/
```

Spring obtiene este valor mediante:

```properties
servicio.login.url=${LOGIN_SERVICE_URL}
```

Esto evita tener que modificar el código cuando se cambia entre desarrollo y producción.

---

## 🗄️ `MONGODB_URI`

Esta variable contiene la conexión hacia MongoDB.

Para trabajar localmente:

```text
MONGODB_URI=mongodb://localhost:27017/notificaciones
```

Para MongoDB Atlas:

```text
MONGODB_URI=mongodb+srv://USUARIO:CONTRASEÑA@CLUSTER/notificaciones
```

Debes reemplazar:

```text
USUARIO
CONTRASEÑA
CLUSTER
```

por los datos correspondientes de tu MongoDB.

> 🔒 No es recomendable subir usuarios, contraseñas ni cadenas de conexión privadas directamente al repositorio.

---

## 📨 `KAFKA_BOOTSTRAP_SERVERS`

Indica la dirección donde se encuentra Apache Kafka.

En desarrollo local:

```text
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

En el `application.properties` utilizamos:

```properties
spring.kafka.bootstrap-servers=${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
```

Esto significa que si no se configura la variable, Spring utilizará automáticamente:

```text
localhost:9092
```

---

# 💻 Configuración en Windows

Si utilizas **CMD**:

```cmd
set LOGIN_SERVICE_URL=http://localhost:8081
set MONGODB_URI=mongodb://localhost:27017/notificaciones
set KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

Luego ejecuta el microservicio desde esa misma consola.

---

Si utilizas **PowerShell**:

```powershell
$env:LOGIN_SERVICE_URL="http://localhost:8081"
$env:MONGODB_URI="mongodb://localhost:27017/notificaciones"
$env:KAFKA_BOOTSTRAP_SERVERS="localhost:9092"
```

---

# 🧪 Configuración completamente local

Para realizar pruebas locales puedes utilizar:

```properties
spring.application.name=notificaciones

server.port=8086

servicio.login.url=http://localhost:8081

spring.data.mongodb.uri=mongodb://localhost:27017/notificaciones

spring.kafka.bootstrap-servers=localhost:9092

spring.kafka.consumer.group-id=notificaciones-service

spring.kafka.consumer.auto-offset-reset=earliest

spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer

spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer
```

Debes tener disponibles:

```text
Login           → http://localhost:8081
Notificaciones  → http://localhost:8086
MongoDB         → localhost:27017
Kafka           → localhost:9092
```

---

# ▶️ Ejecutar el microservicio

Desde la carpeta raíz del proyecto:

```cmd
.\mvnw.cmd spring-boot:run
```

Para compilar sin ejecutar pruebas:

```cmd
.\mvnw.cmd clean package -DskipTests
```

El archivo generado se encontrará normalmente dentro de:

```text
target/
```

Después puedes ejecutarlo con:

```cmd
java -jar target/notificaciones.jar
```

El nombre exacto del archivo `.jar` puede variar según la configuración del `pom.xml`.

---

# 🔎 Verificar que funciona

El microservicio utiliza por defecto el puerto:

```text
8086
```

Por lo tanto, la URL base será:

```text
http://localhost:8086
```

Si aparece un mensaje parecido a:

```text
Tomcat started on port 8086
```

significa que el servicio HTTP inició correctamente.

Sin embargo, en este microservicio también es importante verificar que **Kafka se haya conectado correctamente**, ya que gran parte de su funcionamiento depende del consumo de eventos.

---

# 📨 ¿Cómo funciona Kafka en Notificaciones?

Este microservicio está configurado como **consumidor Kafka**.

Un flujo típico puede ser:

```text
Pagos
  │
  │ publica evento
  ▼
Kafka
  │
  │ consume evento
  ▼
Notificaciones
  │
  ▼
Guarda / procesa notificación
```

Otros microservicios también pueden publicar eventos:

```text
Matrícula ───────┐
                 │
Académico ───────┼──► Kafka ───► Notificaciones
                 │
Pagos ───────────┘
```

---

# 👥 `group-id`

La propiedad:

```properties
spring.kafka.consumer.group-id=notificaciones-service
```

identifica al grupo de consumidores al que pertenece este microservicio.

Esto permite que Kafka sepa qué mensajes ya fueron procesados por el servicio.

El nombre configurado es:

```text
notificaciones-service
```

---

# ⏮️ `auto-offset-reset=earliest`

La configuración:

```properties
spring.kafka.consumer.auto-offset-reset=earliest
```

indica que, si Kafka no tiene registrado un offset previo para este consumidor, empezará a leer desde los mensajes más antiguos disponibles en el tópico.

Esto puede ser útil cuando se inicia el microservicio por primera vez.

---

# 🔤 Deserialización de mensajes

Actualmente tienes:

```properties
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer

spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer
```

Esto significa que Kafka espera recibir:

```text
Key   → String
Value → String
```

Por ejemplo, un mensaje recibido podría tener como contenido:

```json
{
  "tipo": "PAGO_REGISTRADO",
  "usuarioId": "12345",
  "mensaje": "Tu pago fue registrado correctamente"
}
```

Kafka lo entregará inicialmente como un `String`.

Después el microservicio puede convertir ese texto JSON al objeto Java correspondiente.

---

# ⚠️ Importante sobre productores y consumidores

En otros microservicios puedes tener productores configurados con:

```properties
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
```

Mientras que Notificaciones recibe:

```properties
spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer
```

Esto puede funcionar si el mensaje JSON llega correctamente como texto.

Sin embargo, productor y consumidor deben acordar la misma estructura del mensaje.

Por ejemplo:

```text
Productor:
PagoEvento → JSON

Kafka:
{"usuarioId":"123","estado":"PAGADO"}

Consumidor:
String → convertir a PagoEvento
```

---

# 🗄️ MongoDB

La conexión a MongoDB se configura mediante:

```properties
spring.data.mongodb.uri=${MONGODB_URI}
```

Si aparece:

```text
MongoTimeoutException
```

o:

```text
Connection refused localhost:27017
```

revisa:

- que MongoDB esté iniciado;
- que `MONGODB_URI` esté configurado;
- que usuario y contraseña sean correctos;
- que el clúster permita conexiones;
- que la IP esté permitida en MongoDB Atlas;
- que en producción no estés apuntando accidentalmente a `localhost`.

---

# ❌ Error común con Kafka

Si aparece un error similar a:

```text
Connection to node -1 could not be established
```

o:

```text
Bootstrap broker localhost:9092 disconnected
```

normalmente significa que Kafka no está disponible en:

```text
localhost:9092
```

Debes verificar que Kafka esté ejecutándose o configurar:

```text
KAFKA_BOOTSTRAP_SERVERS
```

con la dirección correcta.

---

# 📌 Puertos principales del proyecto

| Servicio | Puerto |
|---|---:|
| Login | `8081` |
| Matrícula | `8082` |
| Académico | `8083` |
| Pagos | `8084` |
| Reportes | `8085` |
| Notificaciones | `8086` |
| MongoDB | `27017` |
| Kafka | `9092` |

---

# 📁 Estructura general

Una estructura típica puede ser:

```text
notificaciones/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── ...
│   │   │
│   │   └── resources/
│   │       └── application.properties
│   │
│   └── test/
│
├── pom.xml
├── mvnw
├── mvnw.cmd
└── README.md
```

---

# ✅ `application.properties` final recomendado

```properties
# ==========================================
# MICROSERVICIO DE NOTIFICACIONES
# ==========================================

spring.application.name=notificaciones

server.port=${PORT:8086}


# ==========================================
# LOGIN
# ==========================================

servicio.login.url=${LOGIN_SERVICE_URL}


# ==========================================
# MONGODB
# ==========================================

spring.data.mongodb.uri=${MONGODB_URI}


# ==========================================
# KAFKA
# ==========================================

spring.kafka.bootstrap-servers=${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}


# ==========================================
# CONSUMIDOR KAFKA
# ==========================================

spring.kafka.consumer.group-id=notificaciones-service

spring.kafka.consumer.auto-offset-reset=earliest

spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer

spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer
```

Con esta configuración, el microservicio **Notificaciones** queda preparado para ejecutarse tanto en **desarrollo local** como en **producción**, utilizando MongoDB y escuchando eventos publicados en Kafka por los demás microservicios del sistema.