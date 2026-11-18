# 🎓 Microservicio Académico

Este microservicio se encarga de gestionar la información relacionada con la parte **académica del sistema**, como cursos, asignaturas, docentes, notas u otros procesos académicos definidos dentro del proyecto.

Está desarrollado con **Spring Boot**, utiliza **MongoDB** como base de datos y puede comunicarse con otros microservicios mediante **Apache Kafka**. También se conecta con el microservicio de **Login** para realizar validaciones de autenticación.

---

## 🚀 Configuración inicial

El archivo principal de configuración se encuentra en:

```text
src/main/resources/application.properties
```

Actualmente tienes algo similar a:

```properties
spring.application.name=academico
server.port=8083
servicio.login.url=spring.data.mongodb.uri=
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
```

Hay que corregir esta línea:

```properties
servicio.login.url=spring.data.mongodb.uri=
```

porque en realidad son **dos propiedades diferentes**.

La configuración correcta sería:

```properties
servicio.login.url=${LOGIN_SERVICE_URL}
spring.data.mongodb.uri=${MONGODB_URI}
```

---

# ⚙️ Configuración recomendada

Una configuración más limpia y preparada para trabajar tanto en local como en producción sería:

```properties
# ==========================================
# MICROSERVICIO ACADÉMICO
# ==========================================

spring.application.name=academico

server.port=${PORT:8083}


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

spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer

spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
```

---

# 🔐 Variables de entorno

Para ejecutar el microservicio se recomienda configurar las siguientes variables.

## `LOGIN_SERVICE_URL`

Indica dónde se encuentra el microservicio encargado del Login y autenticación.

Para desarrollo local:

```text
LOGIN_SERVICE_URL=http://localhost:8081
```

Si el Login se encuentra desplegado en Render:

```text
LOGIN_SERVICE_URL=https://login-microservice-gfy0.onrender.com/
```

Entonces Spring utilizará automáticamente esta variable mediante:

```properties
servicio.login.url=${LOGIN_SERVICE_URL}
```

---

## `MONGODB_URI`

Contiene la conexión hacia MongoDB.

Para trabajar con MongoDB local:

```text
MONGODB_URI=mongodb://localhost:27017/academico
```

Para utilizar MongoDB Atlas:

```text
MONGODB_URI=mongodb+srv://USUARIO:CONTRASEÑA@CLUSTER/academico
```

Debes reemplazar:

```text
USUARIO
CONTRASEÑA
CLUSTER
```

por los datos correspondientes de tu MongoDB.

> 🔒 No se recomienda subir la contraseña o URI completa de MongoDB directamente al repositorio de GitHub.

---

## `KAFKA_BOOTSTRAP_SERVERS`

Esta variable indica la dirección donde se encuentra Apache Kafka.

Para desarrollo local:

```text
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

En nuestro `application.properties` tenemos:

```properties
spring.kafka.bootstrap-servers=${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
```

Esto significa que si no se configura la variable, Spring utilizará automáticamente:

```text
localhost:9092
```

---

# 💻 Configuración en Windows

Si utilizas **CMD**, puedes configurar las variables temporalmente de esta manera:

```cmd
set LOGIN_SERVICE_URL=http://localhost:8081
set MONGODB_URI=mongodb://localhost:27017/academico
set KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

Luego puedes ejecutar el proyecto en esa misma consola.

---

Si utilizas **PowerShell**:

```powershell
$env:LOGIN_SERVICE_URL="http://localhost:8081"
$env:MONGODB_URI="mongodb://localhost:27017/academico"
$env:KAFKA_BOOTSTRAP_SERVERS="localhost:9092"
```

---

# 🧪 Configuración completamente local

Si solo necesitas levantar todo en tu computadora para realizar pruebas, puedes utilizar:

```properties
spring.application.name=academico

server.port=8083

servicio.login.url=http://localhost:8081

spring.data.mongodb.uri=mongodb://localhost:27017/academico

spring.kafka.bootstrap-servers=localhost:9092

spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer

spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
```

En este caso debes tener disponibles:

```text
Login       → http://localhost:8081
Académico   → http://localhost:8083
MongoDB     → localhost:27017
Kafka       → localhost:9092
```

---

# 🌐 Configuración para producción

Para producción es recomendable evitar URLs y credenciales escritas directamente en el código.

Utiliza:

```properties
spring.application.name=academico

server.port=${PORT:8083}

servicio.login.url=${LOGIN_SERVICE_URL}

spring.data.mongodb.uri=${MONGODB_URI}

spring.kafka.bootstrap-servers=${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}

spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer

spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
```

Y configura las variables correspondientes en el servidor:

```text
LOGIN_SERVICE_URL=https://login-microservice-gfy0.onrender.com/

MONGODB_URI=mongodb+srv://usuario:password@cluster/academico

KAFKA_BOOTSTRAP_SERVERS=servidor-kafka:9092
```

---

# ▶️ Ejecutar el microservicio

Desde la carpeta raíz del proyecto puedes ejecutar:

```cmd
.\mvnw.cmd spring-boot:run
```

---

También puedes compilar el proyecto sin ejecutar las pruebas:

```cmd
.\mvnw.cmd clean package -DskipTests
```

Después de compilar, Maven generará el archivo `.jar` dentro de:

```text
target/
```

Puedes ejecutarlo utilizando:

```cmd
java -jar target/academico.jar
```

El nombre exacto del archivo puede variar dependiendo de la configuración del `pom.xml`.

---

# 🔎 Verificar que el servicio funciona

El microservicio Académico utiliza por defecto el puerto:

```text
8083
```

Por lo tanto, la URL base será:

```text
http://localhost:8083
```

Si en la consola aparece algo parecido a:

```text
Tomcat started on port 8083
```

significa que el microservicio inició correctamente.

---

# 🔗 Comunicación con Login

El microservicio Académico puede consultar al microservicio Login para validar usuarios o tokens.

La comunicación sería aproximadamente:

```text
Frontend
   │
   ▼
Académico
http://localhost:8083
   │
   │ Validación de usuario/token
   ▼
Login
http://localhost:8081
```

La URL no debe escribirse directamente dentro del código Java.

Debe obtenerse desde:

```properties
servicio.login.url=${LOGIN_SERVICE_URL}
```

---

# 🗄️ MongoDB

La conexión con MongoDB se configura mediante:

```properties
spring.data.mongodb.uri=${MONGODB_URI}
```

Si al iniciar aparecen errores como:

```text
MongoTimeoutException
```

o:

```text
Connection refused localhost:27017
```

revisa lo siguiente:

- MongoDB debe estar encendido si trabajas localmente.
- `MONGODB_URI` debe estar correctamente configurado.
- Usuario y contraseña de MongoDB deben ser correctos.
- La base de datos debe ser accesible desde tu computadora o servidor.
- Si utilizas MongoDB Atlas, verifica las reglas de acceso de red.
- Si despliegas en Render, no debes usar `localhost:27017` a menos que MongoDB realmente esté ejecutándose dentro del mismo servidor.

---

# 📨 Apache Kafka

Kafka permite que los microservicios puedan enviarse eventos sin depender directamente unos de otros.

Actualmente el productor está configurado con:

```properties
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer

spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
```

Esto significa que:

- Las claves de los mensajes se envían como `String`.
- Los objetos Java se convierten automáticamente a JSON.

La dirección de Kafka se obtiene de:

```properties
spring.kafka.bootstrap-servers=${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
```

---

# 📌 Puertos principales del proyecto

| Microservicio / Servicio | Puerto |
|---|---:|
| Login | `8081` |
| Matrícula | `8082` |
| Académico | `8083` |
| MongoDB | `27017` |
| Kafka | `9092` |

---

# 📁 Estructura general

Una estructura típica del microservicio puede ser:

```text
academico/
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

# ✅ Configuración final recomendada

Puedes dejar tu `application.properties` así:

```properties
# ==========================================
# MICROSERVICIO ACADÉMICO
# ==========================================

spring.application.name=academico

server.port=${PORT:8083}


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

spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer

spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
```

De esta manera, el microservicio **Académico** queda preparado para ejecutarse tanto en **desarrollo local** como en **producción**, sin tener que cambiar URLs ni credenciales directamente dentro del proyecto.