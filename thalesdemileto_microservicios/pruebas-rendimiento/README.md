# Pruebas reproducibles de carga con Apache JMeter

Modulo independiente: no modifica dependencias ni configuraciones de los microservicios.
JMeter 5.6.3 y jmeter-maven-plugin 3.8.0 estan fijados en pom.xml. El wrapper usa Maven 3.9.14.
El plugin documenta JDK 8 a 17 como rango soportado: para reproducibilidad se recomienda
JDK 17 para el generador, aunque los microservicios requieren su propio JDK 21.
En este entorno se verifico tambien una ejecucion completa con JDK 21.0.10 contra
respuestas HTTP simuladas; esta comprobacion valida la integracion, no la capacidad del sistema.

## Preparar el entorno

1. Levantar una copia de los seis microservicios con una base MongoDB de pruebas.
   Usar login en 8080, matricula en 8082, academico en 8083, pagos en 8084,
   reportes en 8085 y notificaciones en 8086. El compose.yaml actual solo levanta login.
2. Configurar TODOS los servicios con `SERVICIO_LOGIN_URL=http://localhost:8080`
   (en contenedores, usar el nombre y puerto interno de login).
   Varios application.properties actuales apuntan a login en Render: sustituir ese
   destino en el entorno de pruebas antes de generar carga. Cada consulta protegida
   provoca una validacion adicional en login.
3. Cargar datos sinteticos representativos. Registrar conteos reales por coleccion,
   tamano de base, indices, versiones, hardware y limites de recursos en una copia
   de entorno.example.json. No usar el pequeno seeder como evidencia de gran volumen.
   Ejemplo de conteo en mongosh: `db.getCollection('nombre_real').countDocuments({})`.
4. Obtener un JWT de una cuenta local de pruebas mediante POST /api/auth/login.
   El token debe seguir vigente durante TODA la ejecucion; renovar entre escenarios
   si su duracion no alcanza. No aumentar la vigencia de produccion para estas pruebas.

Desde PowerShell, en la raiz del repositorio:

Si Windows bloquea scripts, invocar cada comando como
`powershell -NoProfile -ExecutionPolicy Bypass -File .\pruebas-rendimiento\Run-LoadTests.ps1 ...`.
Esto afecta solo al proceso iniciado, sin cambiar la politica permanente del equipo.

```powershell
$credential = Get-Credential -Message 'Cuenta del login LOCAL de pruebas'
$body = @{
    username = $credential.UserName
    password = $credential.GetNetworkCredential().Password
} | ConvertTo-Json
$session = Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/api/auth/login' -ContentType 'application/json' -Body $body
$env:LOAD_TOKEN = $session.token
$body = $null

# Primero verificar conectividad, permisos y contenido: 1 usuario, 21 segundos.
.\pruebas-rendimiento\Run-LoadTests.ps1 -Scenario smoke

# Copiar la plantilla y completar los valores reales antes de continuar.
Copy-Item .\pruebas-rendimiento\entorno.example.json .\pruebas-rendimiento\entorno.local.json

# 50, 100, 200, 400 y 800 usuarios, tres repeticiones por nivel.
.\pruebas-rendimiento\Run-LoadTests.ps1 -Scenario carga -Repetitions 3 -InventoryPath .\pruebas-rendimiento\entorno.local.json

# Carga superior: 800 y 1200 usuarios. Ejecutar tras analizar la prueba anterior.
.\pruebas-rendimiento\Run-LoadTests.ps1 -Scenario estres -InventoryPath .\pruebas-rendimiento\entorno.local.json

# Variante breve exploratoria; no equivale al protocolo completo.
.\pruebas-rendimiento\Run-LoadTests.ps1 -Scenario carga -RampSeconds 60 -WarmupSeconds 30 -HoldSeconds 120 -PauseMilliseconds 100 -InventoryPath .\pruebas-rendimiento\entorno.local.json
```

La primera ejecucion descarga las dependencias desde Maven Central. Si Maven no puede
crear el repositorio local predeterminado, configurar una ubicacion escribible, por ejemplo
`$env:MAVEN_OPTS = '-Dmaven.repo.local=C:/ruta/escribible/maven-cache'` antes de ejecutar.
Para configurar sin generar carga: `./pruebas-rendimiento/mvnw.cmd -f pruebas-rendimiento/pom.xml jmeter:configure`.
En Linux/macOS se puede usar `./mvnw` y el mismo pom/plan; el ejecutor y resumen incluidos son PowerShell.

## Modelo de carga y alcance

- Cada usuario repite cinco GET secuenciales: estudiantes, docentes, deudas,
  indicadores y plantillas. La mezcla converge aproximadamente a 20 % por operacion.
- Se exige HTTP 200 y un arreglo JSON parseable. Un arreglo vacio pasa esta validacion;
  no demuestra suficiencia ni representatividad del volumen de datos.
- Pausa predeterminada: 1000 ms antes de CADA solicitud. Con 800 usuarios y respuestas
  muy rapidas, el limite aproximado es 800 solicitudes/s; con respuestas mas lentas
  disminuye. Los usuarios virtuales no son solicitudes/s ni navegadores completos.
- Se comparte un JWT: se simulan clientes concurrentes, no 800 cuentas diferentes.
  No se mide el proceso de login masivo, escrituras, pagos externos ni frontend.
- Por nivel: rampa de 300 s, calentamiento de 60 s y periodo sostenido de 900 s.
  El total es 1260 s; quince ejecuciones requieren aproximadamente 5 h 15 min
  mas preparacion. Empezar con smoke y la variante breve.
- El resumen excluye rampa y calentamiento tomando como referencia la primera muestra
  HTTP registrada. El informe HTML de JMeter incluye toda la ejecucion.
- Los cinco endpoints listan colecciones completas: el costo depende del volumen.
  No extrapolar estos resultados a operaciones de negocio no evaluadas.

## Evidencia y criterios

Cada ejecucion tiene un directorio unico `target/<identificador>/` con:

- `entorno.json`: parametros, revision Git, estado del arbol, inventario declarado
  y datos basicos del generador. Nunca guarda el token.
- `maven.log` y `jmeter/results/*.csv`: registro y muestras originales, con errores.
- `jmeter/reports/`: informe HTML de JMeter, con graficas y percentiles.
- `resumen.csv` y `resumen.json`: media, p95, p99, maximo, errores y solicitudes/s
  globales y por operacion, despues de excluir el periodo inicial.

Los percentiles del resumen usan rango mas cercano y pueden diferir del algoritmo
de interpolacion del dashboard. La media incluye respuestas fallidas; los errores
siempre se reportan aparte. Solicitudes/s incluye todas las muestras del intervalo.

Umbrales iniciales propuestos: p95 <= 1000 ms y errores <= 1 % en CADA operacion y
globalmente. Se verifica tambien que el pico de usuarios activos llegue al objetivo;
ese pico por si solo no prueba que la concurrencia se haya mantenido todo el periodo:
revisar la grafica Active Threads y los registros originales. Los umbrales se pueden
ajustar con -P95LimitMs y -ErrorLimitPercent y deben justificarse antes de medir.
El control de errores de Maven conserva ademas un limite global del 1 % para toda
la corrida (incluye rampa y calentamiento).

Un incumplimiento produce codigo de salida 1 y conserva la evidencia. No se alteran
ni se descartan muestras para obtener un resultado favorable. Registrar tambien
CPU, RAM, disco y red del servidor y del generador con monitoreo externo durante
la ejecucion: el modulo no recoge automaticamente esas series temporales.

No atribuir 185 ms ni 99.8 % a estas pruebas sin los registros que los respalden.
Los valores de DataSeeder en reportes son datos iniciales, no mediciones.
El porcentaje de solicitudes exitosas no mide disponibilidad temporal.
Para comprobar el calculo del resumen con muestras artificiales:
`powershell -NoProfile -ExecutionPolicy Bypass -File .\pruebas-rendimiento\Test-Summary.ps1`.
Archivar los directorios de resultados fuera de target antes de limpiar o entregar
anexos: target esta excluido de Git.

Referencias tecnicas:
- https://jmeter.apache.org/usermanual/best-practices.html
- https://github.com/jmeter-maven-plugin/jmeter-maven-plugin
- https://github.com/jmeter-maven-plugin/jmeter-maven-plugin/wiki/Generating-Reports
