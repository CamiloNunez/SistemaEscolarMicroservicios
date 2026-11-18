# 📊 Microservicio de Reportes

Este microservicio se encarga de gestionar la **generación, consulta y procesamiento de reportes** dentro del sistema.

Está desarrollado con **Spring Boot**, utiliza **MongoDB** para almacenar información relacionada con los reportes y se integra con otros microservicios mediante **Apache Kafka**.

Además, se comunica con el microservicio de **Login** para realizar validaciones de autenticación.

---

## 🚀 Configuración inicial

El archivo principal de configuración se encuentra en:

```text
src/main/resources/application.properties
```

Actualmente tienes la siguiente configuración:

```properties
spring.application.name=reportes
server.port=8085

servicio.login.url=https://login-microservice-gfy0.onrender.com/

spring.data.mongodb.uri=

spring.kafka.bootstrap-servers=localhost:9092

spring.kafka.consumer.group-id=reportes-service
spring.kafka.consumer.auto-offset-reset=earliest

spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer
```

Esta configuración funciona como base, pero para producción se recomienda utilizar **variables de entorno**, especialmente para MongoDB, Kafka y la URL del microservicio de Login.

---

# ⚙️ Configuración recomendada

El `application.properties` puede quedar de la siguiente manera:

```properties
# ==========================================
# MICROSERVICIO DE REPORTES
# ==========================================

spring.application.name=reportes

server.port=${PORT:8085}


# ==========================================
# MICROSERVICIO DE LOGIN
# ==========================================

servicio.login.url=${LOGIN_SERVICE_URL:https://login-microservice-gfy0.onrender.com/}


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

spring.kafka.consumer.group-id=reportes-service

spring.kafka.consumer.auto-offset-reset=earliest

spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer

spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer
```

De esta manera, la aplicación utiliza valores configurables según el entorno.

---

# 🔐 Variables de entorno

Las principales variables que utiliza este microservicio son:

```text
LOGIN_SERVICE_URL
MONGODB_URI
KAFKA_BOOTSTRAP_SERVERS
```

---

## 🔑 `LOGIN_SERVICE_URL`

Esta variable indica dónde está disponible el microservicio encargado del Login.

Actualmente el proyecto utiliza:

```text
https://login-microservice-gfy0.onrender.com/
```

Por eso puedes configurar:

```text
LOGIN_SERVICE_URL=https://login-microservice-gfy0.onrender.com/
```

En el `application.properties` utilizamos:

```properties
servicio.login.url=${LOGIN_SERVICE_URL:https://login-microservice-gfy0.onrender.com/}
```

El valor después de `:` funciona como valor por defecto.

Por lo tanto, si `LOGIN_SERVICE_URL` no está definida, Spring utilizará automáticamente:

```text
https://login-microservice-gfy0.onrender.com/
```

---

## 🗄️ `MONGODB_URI`

Esta variable contiene la conexión hacia MongoDB.

Para trabajar localmente:

```text
MONGODB_URI=mongodb://localhost:27017/reportes
```

Si utilizas MongoDB Atlas:

```text
MONGODB_URI=mongodb+srv://USUARIO:CONTRASEÑA@CLUSTER/reportes
```

Debes reemplazar:

```text
USUARIO
CONTRASEÑA
CLUSTER
```

con los datos correspondientes de tu base de datos.

> 🔒 No se recomienda colocar la contraseña de MongoDB directamente dentro del `application.properties` si el proyecto será subido a GitHub.

---

## 📨 `KAFKA_BOOTSTRAP_SERVERS`

Esta variable indica dónde se encuentra ejecutándose Apache Kafka.

En desarrollo local normalmente será:

```text
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

En el proyecto utilizamos:

```properties
spring.kafka.bootstrap-servers=${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
```

Si la variable no existe, se utilizará automáticamente:

```text
localhost:9092
```

---

# 💻 Configurar variables en Windows

Si utilizas **CMD**:

```cmd
set LOGIN_SERVICE_URL=https://login-microservice-gfy0.onrender.com/
set MONGODB_URI=mongodb://localhost:27017/reportes
set KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

Después puedes ejecutar el microservicio desde esa misma ventana.

---

Si utilizas **PowerShell**:

```powershell
$env:LOGIN_SERVICE_URL="https://login-microservice-gfy0.onrender.com/"
$env:MONGODB_URI="mongodb://localhost:27017/reportes"
$env:KAFKA_BOOTSTRAP_SERVERS="localhost:9092"
```

---

# 🧪 Configuración para desarrollo local

Si deseas probar Reportes en tu computadora, puedes utilizar:

```properties
spring.application.name=reportes

server.port=8085

servicio.login.url=https://login-microservice-gfy0.onrender.com/

spring.data.mongodb.uri=mongodb://localhost:27017/reportes

spring.kafka.bootstrap-servers=localhost:9092

spring.kafka.consumer.group-id=reportes-service

spring.kafka.consumer.auto-offset-reset=earliest

spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer

spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer
```

En este caso debes tener disponibles:

```text
Reportes     → http://localhost:8085
Login        → https://login-microservice-gfy0.onrender.com/
MongoDB      → localhost:27017
Kafka        → localhost:9092
```

---

# 🌐 Configuración para producción

En producción es recomendable utilizar:

```properties
spring.application.name=reportes

server.port=${PORT:8085}

servicio.login.url=${LOGIN_SERVICE_URL:https://login-microservice-gfy0.onrender.com/}

spring.data.mongodb.uri=${MONGODB_URI}

spring.kafka.bootstrap-servers=${KAFKA_BOOTSTRAP_SERVERS}

spring.kafka.consumer.group-id=reportes-service

spring.kafka.consumer.auto-offset-reset=earliest

spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer

spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer
```

En el servidor se deben configurar variables similares a:

```text
LOGIN_SERVICE_URL=https://login-microservice-gfy0.onrender.com/

MONGODB_URI=mongodb+srv://usuario:password@cluster/reportes

KAFKA_BOOTSTRAP_SERVERS=servidor-kafka:9092
```

---

# ▶️ Ejecutar el microservicio

Desde la carpeta raíz del proyecto puedes ejecutar:

```cmd
.\mvnw.cmd spring-boot:run
```

---

Para compilar sin ejecutar las pruebas:

```cmd
.\mvnw.cmd clean package -DskipTests
```

El `.jar` generado normalmente se encontrará dentro de:

```text
target/
```

Luego puedes ejecutarlo con:

```cmd
java -jar target/reportes.jar
```

El nombre exacto puede variar según la configuración del `pom.xml`.

---

# 🔎 Verificar que Reportes funciona

El microservicio utiliza el puerto:

```text
8085
```

Por lo tanto, su dirección base en local será:

```text
http://localhost:8085
```

Si en consola aparece algo similar a:

```text
Tomcat started on port 8085
```

el servidor HTTP se inició correctamente.

También debes comprobar que no existan errores de conexión con:

```text
MongoDB
Kafka
Login
```

---

# 🔗 Comunicación con Login

El microservicio de Reportes utiliza:

```properties
servicio.login.url=${LOGIN_SERVICE_URL:https://login-microservice-gfy0.onrender.com/}
```

para conocer la dirección del servicio de autenticación.

El flujo puede ser:

```text
Frontend
   │
   ▼
Reportes
http://localhost:8085
   │
   │ Validación de token / usuario
   ▼
Login
https://login-microservice-gfy0.onrender.com/
```

Esto permite que Reportes no tenga que implementar nuevamente toda la lógica de autenticación.

---

# 📨 Kafka en el microservicio de Reportes

Este microservicio está configurado principalmente como **consumidor de eventos Kafka**.

Un ejemplo de arquitectura sería:

```text
Matrícula ───────┐
                 │
Académico ───────┼────► Kafka ─────► Reportes
                 │
Pagos ───────────┘
```

Los demás microservicios pueden publicar eventos y Reportes puede utilizarlos para actualizar información necesaria para generar estadísticas o reportes.

---

# 👥 Grupo de consumidores

La propiedad:

```properties
spring.kafka.consumer.group-id=reportes-service
```

indica que el consumidor pertenece al grupo:

```text
reportes-service
```

Kafka utiliza este identificador para administrar los mensajes procesados por el microservicio.

---

# ⏮️ `auto-offset-reset=earliest`

La configuración:

```properties
spring.kafka.consumer.auto-offset-reset=earliest
```

indica que, si Kafka no encuentra un offset previamente guardado para este grupo, empezará a consumir desde los mensajes más antiguos disponibles.

Esto es especialmente útil cuando el microservicio se inicia por primera vez.

---

# 🔤 Deserializadores de Kafka

Actualmente la configuración es:

```properties
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer

spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer
```

Esto significa que Reportes recibe tanto la clave como el contenido del mensaje como:

```text
String
```

Por ejemplo, Kafka podría recibir:

```json
{
  "tipo": "PAGO_REGISTRADO",
  "estudianteId": "123",
  "monto": 250.00
}
```

El microservicio lo recibe como texto y posteriormente puede convertirlo a un objeto Java.

---

# 📊 Ejemplo de flujo de reportes

Un flujo posible dentro del sistema puede ser:

```text
Pagos
   │
   │ publica pago realizado
   ▼
Kafka
   │
   ▼
Reportes
   │
   │ procesa información
   ▼
MongoDB
   │
   ▼
Dashboard / Reporte
```

De esta manera el módulo de Reportes puede recopilar información producida por otros servicios.

---

# 🗄️ MongoDB

La conexión se configura mediante:

```properties
spring.data.mongodb.uri=${MONGODB_URI}
```

Si aparece un error como:

```text
MongoTimeoutException
```

o:

```text
Connection refused localhost:27017
```

debes revisar:

- que MongoDB esté funcionando;
- que `MONGODB_URI` esté configurada correctamente;
- que el usuario y contraseña sean válidos;
- que la IP tenga acceso al clúster de MongoDB Atlas;
- que el servidor no esté intentando conectarse a `localhost` por error.

---

# ❌ Problemas comunes con Kafka

Si aparece:

```text
Connection to node -1 could not be established
```

o:

```text
Bootstrap broker localhost:9092 disconnected
```

significa generalmente que Kafka no se encuentra disponible en:

```text
localhost:9092
```

Debes comprobar que Kafka esté encendido o configurar correctamente:

```text
KAFKA_BOOTSTRAP_SERVERS
```

---

# 📌 Puertos principales del sistema

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

Una estructura típica del proyecto puede ser:

```text
reportes/
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
# MICROSERVICIO DE REPORTES
# ==========================================

spring.application.name=reportes

server.port=${PORT:8085}


# ==========================================
# LOGIN
# ==========================================

servicio.login.url=${LOGIN_SERVICE_URL:https://login-microservice-gfy0.onrender.com/}


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

spring.kafka.consumer.group-id=reportes-service

spring.kafka.consumer.auto-offset-reset=earliest

spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer

spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer
```

Con esta configuración, el microservicio **Reportes** queda preparado para trabajar tanto en **desarrollo local** como en **producción**, comunicándose con Login, utilizando MongoDB y consumiendo eventos de los demás microservicios mediante Apache Kafka.