param(
    [ValidateSet('smoke', 'carga', 'estres')][string]$Scenario = 'smoke',
    [ValidateRange(1, 10)][int]$Repetitions = 1,
    [ValidateRange(1, 3600)][int]$RampSeconds = 300,
    [ValidateRange(0, 3600)][int]$WarmupSeconds = 60,
    [ValidateRange(1, 86400)][int]$HoldSeconds = 900,
    [ValidateRange(0, 60000)][int]$PauseMilliseconds = 1000,
    [string]$InventoryPath,
    [ValidateRange(1, 60000)][int]$P95LimitMs = 1000,
    [ValidateRange(0, 100)][double]$ErrorLimitPercent = 1
)
$ErrorActionPreference = 'Stop'
$module = $PSScriptRoot
$repo = Split-Path $module -Parent
$wrapper = Join-Path $module 'mvnw.cmd'
if ([string]::IsNullOrWhiteSpace($env:LOAD_TOKEN)) {
    throw 'Define LOAD_TOKEN con un JWT vigente del login LOCAL de pruebas. Ver README.md.'
}
$users = switch ($Scenario) {
    'smoke' { @(1) }
    'carga' { @(50, 100, 200, 400, 800) }
    'estres' { @(800, 1200) }
}
if ($Scenario -eq 'smoke') { $RampSeconds = 1; $WarmupSeconds = 0; $HoldSeconds = 20 }
if ($Scenario -ne 'smoke' -and -not $InventoryPath) {
    throw 'Indica -InventoryPath con el inventario real de la base de pruebas (ver entorno.example.json).'
}
$inventory = $null
if ($InventoryPath) {
    $inventory = Get-Content -LiteralPath $InventoryPath -Raw | ConvertFrom-Json
    if (-not $inventory.collections -or -not $inventory.environment) { throw 'Inventario sin environment o collections.' }
    foreach ($collection in $inventory.collections) {
        if (-not $collection.name -or $null -eq $collection.count -or $collection.count -lt 0) {
            throw 'Cada coleccion debe tener name y count real, no negativo.'
        }
    }
}
# No enviar trafico a los despliegues publicos. Los servicios deben validar contra login local.
$endpoints = @(
    'http://localhost:8082/api/estudiantes',
    'http://localhost:8083/api/docentes',
    'http://localhost:8084/api/deudas',
    'http://localhost:8085/api/indicadores',
    'http://localhost:8086/api/plantillas-notificacion'
)
foreach ($endpoint in $endpoints) {
    $response = Invoke-WebRequest -UseBasicParsing -Uri $endpoint -TimeoutSec 20 -Headers @{ Authorization = "Bearer $env:LOAD_TOKEN" }
    if ($response.StatusCode -ne 200 -or -not $response.Content.TrimStart().StartsWith('[')) {
        throw "Preflight fallo: $endpoint no devolvio HTTP 200 con arreglo JSON."
    }
    $null = ConvertFrom-Json -InputObject $response.Content
}
$revision = & git -C $repo rev-parse HEAD
$dirty = @(& git -C $repo status --porcelain).Count -gt 0
$allPassed = $true
foreach ($count in $users) {
    for ($rep = 1; $rep -le $Repetitions; $rep++) {
        $run = '{0}-{1}-u{2}-r{3}-{4}' -f (Get-Date -Format 'yyyyMMdd-HHmmss'), $Scenario, $count, $rep, ([guid]::NewGuid().ToString('N').Substring(0,8))
        $output = Join-Path $module "target/$run"
        New-Item -ItemType Directory -Path $output -Force | Out-Null
        $start = (Get-Date).ToUniversalTime()
        $metadata = [ordered]@{
            run = $run; startedUtc = $start.ToString('o'); scenario = $Scenario
            users = $count; rampSeconds = $RampSeconds; warmupSeconds = $WarmupSeconds
            holdSeconds = $HoldSeconds; pauseMilliseconds = $PauseMilliseconds
            repetitionsRequested = $Repetitions; repetition = $rep
            p95LimitMs = $P95LimitMs; errorLimitPercent = $ErrorLimitPercent
            revision = "$revision"; workingTreeDirty = $dirty; endpoints = $endpoints
            authentication = 'JWT compartido; usuarios virtuales, no cuentas distintas'
            inventory = $inventory
            generator = @{ os = [Environment]::OSVersion.VersionString; processors = [Environment]::ProcessorCount; java = ((& java --version) -join "`n") }
        }
        $metadata | ConvertTo-Json -Depth 12 | Set-Content -LiteralPath (Join-Path $output 'entorno.json') -Encoding UTF8
        $duration = $RampSeconds + $WarmupSeconds + $HoldSeconds
        $mavenArgs = @('-B', '-f', (Join-Path $module 'pom.xml'), "-Dmaven.repo.local=$module/target/maven-cache", "-Dload.run=$run",
            "-Dload.users=$count", "-Dload.ramp=$RampSeconds", "-Dload.duration=$duration",
            "-Dload.pause=$PauseMilliseconds", 'verify')
        Write-Host "Ejecutando $run ($duration segundos)."
        & $wrapper @mavenArgs 2>&1 | Tee-Object -FilePath (Join-Path $output 'maven.log') | Out-Host
        $mavenExit = $LASTEXITCODE
        $results = @(Get-ChildItem -LiteralPath (Join-Path $output 'jmeter/results') -Filter '*.csv' -ErrorAction SilentlyContinue)
        if ($results.Count -ne 1) { throw "No se obtuvo un archivo de resultados unico: $output (Maven: $mavenExit)." }
        & (Join-Path $module 'Summarize-Results.ps1') -ResultsPath $results[0].FullName -OutputDirectory $output `
            -ExcludeInitialSeconds ($RampSeconds + $WarmupSeconds) -ExpectedUsers $count `
            -ExpectedOperations 5 -P95LimitMs $P95LimitMs -ErrorLimitPercent $ErrorLimitPercent
        if ($LASTEXITCODE -ne 0 -or $mavenExit -ne 0) { $allPassed = $false }
    }
}
if (-not $allPassed) { Write-Host 'Hay escenarios que incumplen criterios. Revisa resumen.json y maven.log.'; exit 1 }
Write-Host 'Escenarios completados. Revisa los resultados y las limitaciones del protocolo.'
exit 0
