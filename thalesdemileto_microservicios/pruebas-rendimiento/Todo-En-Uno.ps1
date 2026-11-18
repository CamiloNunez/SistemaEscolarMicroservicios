<#
  Automatiza todo el protocolo de pruebas de rendimiento:
    1. Levanta login (docker compose) y los 5 servicios apuntando al login LOCAL.
    2. Espera a que todos respondan.
    3. Pide credenciales y obtiene el token (LOAD_TOKEN).
    4. Prepara el inventario (solo escenario carga/estres).
    5. Ejecuta Run-LoadTests.ps1.
    6. Muestra el resumen y abre el informe HTML.

  Uso:
    .\pruebas-rendimiento\ejecutar.cmd                -> prueba breve (smoke)
    .\pruebas-rendimiento\ejecutar.cmd carga          -> carga 50..800 usuarios x3
    .\pruebas-rendimiento\ejecutar.cmd detener        -> apaga los servicios levantados
#>
param(
    [ValidateSet('smoke', 'carga', 'estres', 'detener')][string]$Scenario = 'smoke',
    [ValidateRange(1, 10)][int]$Repetitions = 3,
    [switch]$NuevoToken
)
$ErrorActionPreference = 'Stop'
$module = $PSScriptRoot
$repo = Split-Path $module -Parent
$logs = Join-Path $module 'target\servicios-logs'

$servicios = [ordered]@{
    matricula      = 8082
    academico      = 8083
    pagos          = 8084
    reportes       = 8085
    notificaciones = 8086
}

function Test-Puerto([int]$Puerto) {
    $null -ne (Get-NetTCPConnection -LocalPort $Puerto -State Listen -ErrorAction SilentlyContinue)
}

function Paso([string]$Texto) { Write-Host "`n=== $Texto" -ForegroundColor Cyan }

# ---------------------------------------------------------------- detener
if ($Scenario -eq 'detener') {
    Paso 'Deteniendo servicios'
    foreach ($s in $servicios.GetEnumerator()) {
        $conns = Get-NetTCPConnection -LocalPort $s.Value -State Listen -ErrorAction SilentlyContinue
        foreach ($c in $conns) {
            Stop-Process -Id $c.OwningProcess -Force -ErrorAction SilentlyContinue
            Write-Host "  $($s.Key) ($($s.Value)) detenido."
        }
    }
    Write-Host '  Login (Docker) se deja encendido. Para apagarlo: docker compose down'
    exit 0
}

# ---------------------------------------------------------------- 1. servicios
Paso '1/6 Levantando servicios'
if (-not (Test-Puerto 8080)) {
    Write-Host '  Login no responde; iniciando con docker compose...'
    & docker compose -f (Join-Path $repo 'compose.yaml') up -d login
    if ($LASTEXITCODE -ne 0) { throw 'No se pudo iniciar login con Docker. ¿Docker Desktop está abierto?' }
} else {
    Write-Host '  login (8080) ya está activo.'
}

New-Item -ItemType Directory -Path $logs -Force | Out-Null
# Los procesos hijos heredan esta variable: los servicios validan contra el login LOCAL.
$env:SERVICIO_LOGIN_URL = 'http://localhost:8080/'
# pagos no arranca sin stripe.secret-key; la prueba solo hace GET /api/deudas y no llama a Stripe.
$env:STRIPE_SECRETKEY = if ($env:STRIPE_SECRET_KEY) { $env:STRIPE_SECRET_KEY } else { 'sk_test_solo_pruebas_rendimiento' }
foreach ($s in $servicios.GetEnumerator()) {
    if (Test-Puerto $s.Value) {
        Write-Host "  $($s.Key) ($($s.Value)) ya está activo. OJO: si lo iniciaste tú, verifica que use el login local."
        continue
    }
    $dir = Join-Path $repo $s.Key
    $log = Join-Path $logs "$($s.Key).log"
    Start-Process -FilePath 'cmd.exe' -WorkingDirectory $dir -WindowStyle Hidden `
        -ArgumentList "/c `"`"$dir\mvnw.cmd`" -q spring-boot:run > `"$log`" 2>&1`""
    Write-Host "  $($s.Key) ($($s.Value)) iniciando... log: $log"
}

# ---------------------------------------------------------------- 2. esperar
Paso '2/6 Esperando a que todos los servicios respondan (máx. 6 min)'
$limite = (Get-Date).AddMinutes(6)
$pendientes = @($servicios.Keys)
while ($pendientes.Count -gt 0) {
    $pendientes = @($pendientes | Where-Object { -not (Test-Puerto $servicios[$_]) })
    if ($pendientes.Count -eq 0) { break }
    if ((Get-Date) -gt $limite) {
        throw "No arrancaron: $($pendientes -join ', '). Revisa los logs en $logs"
    }
    Write-Host "  Esperando: $($pendientes -join ', ')"
    Start-Sleep -Seconds 10
}
Write-Host '  Todos los servicios están arriba.' -ForegroundColor Green

# ---------------------------------------------------------------- 3. token
Paso '3/6 Obteniendo token del login local'
if ($NuevoToken -or [string]::IsNullOrWhiteSpace($env:LOAD_TOKEN)) {
    $archivoCred = Join-Path $module 'credenciales.local.json'
    if (-not (Test-Path $archivoCred)) {
        '{ "usuario": "ESCRIBE_AQUI_TU_USUARIO", "contrasena": "ESCRIBE_AQUI_TU_CONTRASENA" }' |
            Set-Content $archivoCred -Encoding UTF8
    }
    while ((Get-Content $archivoCred -Raw) -match 'ESCRIBE_AQUI') {
        Write-Host '  Escribe tu usuario y contraseña de la aplicación en el Bloc de notas, guarda (Ctrl+G) y ciérralo.' -ForegroundColor Yellow
        Start-Process notepad.exe -ArgumentList "`"$archivoCred`"" -Wait
    }
    $cred = Get-Content $archivoCred -Raw | ConvertFrom-Json
    $cuerpo = @{ username = $cred.usuario; password = $cred.contrasena } | ConvertTo-Json
    try {
        $sesion = Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/api/auth/login' `
            -ContentType 'application/json' -Body $cuerpo
    } catch {
        throw "Login falló: $($_.Exception.Message). Revisa usuario/contraseña."
    } finally { $cuerpo = $null; $cred = $null }
    $env:LOAD_TOKEN = $sesion.token
    if ([string]::IsNullOrWhiteSpace($env:LOAD_TOKEN)) { throw 'El login no devolvió un token.' }
    Write-Host '  Token configurado (válido 24 h).' -ForegroundColor Green
} else {
    Write-Host '  Se reutiliza el LOAD_TOKEN existente (usa -NuevoToken para renovarlo).'
}

# ---------------------------------------------------------------- 4. inventario
$argsPrueba = @{ Scenario = $Scenario }
if ($Scenario -ne 'smoke') {
    Paso '4/6 Inventario del entorno'
    $inv = Join-Path $module 'entorno.local.json'
    if (-not (Test-Path $inv)) {
        $json = Get-Content (Join-Path $module 'entorno.example.json') -Raw | ConvertFrom-Json
        # Autocompletar lo que se puede detectar de este equipo.
        $cpu = (Get-CimInstance Win32_Processor | Select-Object -First 1).Name.Trim()
        $ram = [math]::Round((Get-CimInstance Win32_ComputerSystem).TotalPhysicalMemory / 1GB, 1)
        $os = (Get-CimInstance Win32_OperatingSystem).Caption
        $java = ((& java -version 2>&1) | Select-Object -First 1).ToString()
        $json.environment = "Equipo local $env:COMPUTERNAME, $(Get-Date -Format 'yyyy-MM-dd'), responsable: REEMPLAZAR"
        $json.server.cpu = $cpu; $json.server.ramGB = $ram; $json.server.os = $os
        $json.server.javaVersion = $java
        $json.server.serviceInstancesAndResourceLimits = '1 instancia por servicio (spring-boot:run), sin límites; login en Docker'
        $json.generator.ramGB = $ram
        $json | ConvertTo-Json -Depth 10 | Set-Content $inv -Encoding UTF8
        Write-Host '  Se creó entorno.local.json con CPU, RAM, SO y Java ya rellenados.'
    }
    while ((Get-Content $inv -Raw) -match 'REEMPLAZAR|"count":\s*null') {
        Write-Host '  Completa los campos "REEMPLAZAR" y los conteos de las colecciones; guarda y cierra el Bloc de notas.' -ForegroundColor Yellow
        Start-Process notepad.exe -ArgumentList "`"$inv`"" -Wait
    }
    $argsPrueba.Repetitions = $Repetitions
    $argsPrueba.InventoryPath = $inv
    $horas = [math]::Round(5 * $Repetitions * (300 + 60 + 900) / 3600, 1)
    Write-Host "  Duración estimada: ~$horas horas. No cierres esta ventana ni suspendas el equipo." -ForegroundColor Yellow
} else {
    Paso '4/6 Inventario: no requerido para smoke'
}

# ---------------------------------------------------------------- 5. ejecutar
Paso "5/6 Ejecutando prueba '$Scenario'"
$inicio = Get-Date
& (Join-Path $module 'Run-LoadTests.ps1') @argsPrueba
$codigo = $LASTEXITCODE

# ---------------------------------------------------------------- 6. informe
Paso '6/6 Resultados'
$corridas = @(Get-ChildItem (Join-Path $module 'target') -Directory |
    Where-Object { $_.LastWriteTime -ge $inicio -and (Test-Path (Join-Path $_.FullName 'resumen.json')) } |
    Sort-Object LastWriteTime)
if ($corridas.Count -eq 0) { throw 'No se generó ninguna ejecución con resumen. Revisa la salida anterior.' }
foreach ($c in $corridas) {
    Write-Host "`n  $($c.FullName)"
    Import-Csv (Join-Path $c.FullName 'resumen.csv') | Format-Table -AutoSize | Out-Host
}
$ultima = $corridas[-1]
$informe = Get-ChildItem $ultima.FullName -Recurse -Filter index.html -ErrorAction SilentlyContinue |
    Where-Object { $_.FullName -match '\\reports\\' } | Select-Object -First 1
if ($informe) { Start-Process $informe.FullName } else { Write-Host '  No se encontró informe HTML.' }

if ($codigo -eq 0) { Write-Host "`nListo: todos los escenarios cumplen criterios." -ForegroundColor Green }
else { Write-Host "`nTerminado, pero hay escenarios que incumplen criterios (ver resumen.json / maven.log)." -ForegroundColor Yellow }
Write-Host 'Para apagar los servicios: .\pruebas-rendimiento\ejecutar.cmd detener'
exit $codigo
