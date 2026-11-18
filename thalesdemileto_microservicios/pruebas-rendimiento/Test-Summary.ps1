# Prueba del calculo estadistico; los datos son artificiales, no resultados del sistema.
$ErrorActionPreference = 'Stop'
$testDirectory = Join-Path $PSScriptRoot ('target/test-summary-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $testDirectory -Force | Out-Null
$fixture = Join-Path $testDirectory 'muestras.csv'
@'
timeStamp,elapsed,label,success,allThreads
1000,9999,consulta,true,1
11000,100,consulta,true,800
12000,200,consulta,true,800
13000,300,consulta,true,800
'@ | Set-Content -LiteralPath $fixture -Encoding UTF8
$script = Join-Path $PSScriptRoot 'Summarize-Results.ps1'
function Invoke-Case($name, $limit, $users, $expectedExit) {
    $destination = Join-Path $testDirectory $name
    & powershell -NoProfile -ExecutionPolicy Bypass -File $script -ResultsPath $fixture -OutputDirectory $destination -ExcludeInitialSeconds 10 -ExpectedUsers $users -P95LimitMs $limit
    if ($LASTEXITCODE -ne $expectedExit) { throw "Codigo incorrecto en $name : $LASTEXITCODE" }
    Get-Content -LiteralPath (Join-Path $destination 'resumen.json') -Raw | ConvertFrom-Json
}
$valid = Invoke-Case 'valido' 1000 800 0
if ($valid.metrics[0].meanMs -ne 200 -or $valid.metrics[0].p95Ms -ne 300 -or $valid.excludedSamples -ne 1) {
    throw 'Media, percentil o exclusion incorrectos.'
}
$null = Invoke-Case 'lento' 250 800 1
$null = Invoke-Case 'concurrencia-insuficiente' 1000 1200 1
(Get-Content -LiteralPath $fixture -Raw).Replace('13000,300,consulta,true', '13000,300,consulta,false') | Set-Content -LiteralPath $fixture -Encoding UTF8
$invalid = Invoke-Case 'errores' 1000 800 1
if ($invalid.metrics[0].errors -ne 1 -or $invalid.metrics[0].errorPercent -ne 33.333) { throw 'Conteo de errores incorrecto.' }
Write-Host 'OK: media, percentiles, calentamiento, errores y concurrencia insuficiente.'
exit 0
