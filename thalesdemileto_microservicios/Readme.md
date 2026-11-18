# Pruebas de rendimiento

El modulo [pruebas-rendimiento](pruebas-rendimiento/README.md) integra Apache JMeter
con escenarios de 50 a 800 usuarios y estres de hasta 1200, reportes HTML y resumen
de tiempos y errores. Consultar su guia para preparar el entorno local y ejecutar.

# Ejecutar en Docker

Desde la raíz del repositorio, con Docker Desktop iniciado, ejecuta un solo comando:

```powershell
docker compose up -d --build --wait --wait-timeout 180
```

Este comando compila login, crea su imagen y deja el contenedor ejecutándose en segundo plano. Puedes repetirlo para aplicar cambios.

**Servicio login: http://localhost:8080** (puerto del equipo: **8080**; puerto interno: **10000**).

Esta configuración levanta el microservicio login. Usa la conexión MongoDB de `login/src/main/resources/application.properties`; esa base debe estar accesible para las operaciones que la necesitan.

Para consultar el estado y los puertos:

```powershell
docker compose ps
```

Para consultar los registros:

```powershell
docker compose logs --tail 100 login
```

Para detener el contenedor:

```powershell
docker compose stop
```

Para ejecutar login sin Docker, desde su carpeta:

```powershell
.\mvnw.cmd spring-boot:run
```
