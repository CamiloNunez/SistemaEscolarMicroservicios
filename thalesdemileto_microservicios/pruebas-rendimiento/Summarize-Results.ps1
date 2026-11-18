param(
    [Parameter(Mandatory)][string]$ResultsPath,
    [Parameter(Mandatory)][string]$OutputDirectory,
    [ValidateRange(0, 100000)][int]$ExcludeInitialSeconds = 0,
    [ValidateRange(1, 100000)][int]$ExpectedUsers = 1,
    [ValidateRange(1, 1000)][int]$ExpectedOperations = 1,
    [ValidateRange(1, 60000)][int]$P95LimitMs = 1000,
    [ValidateRange(0, 100)][double]$ErrorLimitPercent = 1
)
$ErrorActionPreference = 'Stop'
$rows = @(Import-Csv -LiteralPath $ResultsPath)
if ($rows.Count -eq 0) { throw 'Sin muestras: no se puede evaluar rendimiento.' }
foreach ($field in @('timeStamp','elapsed','label','success','allThreads')) {
    if ($rows[0].PSObject.Properties.Name -notcontains $field) { throw "Falta columna JMeter: $field" }
}
$first = ($rows | ForEach-Object { [long]$_.timeStamp } | Measure-Object -Minimum).Minimum
$cutoff = $first + 1000L * $ExcludeInitialSeconds
$measured = @($rows | Where-Object { [long]$_.timeStamp -ge $cutoff })
if ($measured.Count -eq 0) { throw 'Sin muestras despues de excluir rampa y calentamiento.' }
function Get-Metrics($name, $samples) {
    $times = @($samples | ForEach-Object { [double]$_.elapsed } | Sort-Object)
    $errors = @($samples | Where-Object { $_.success -ne 'true' }).Count
    $begin = ($samples | ForEach-Object { [long]$_.timeStamp } | Measure-Object -Minimum).Minimum
    $end = ($samples | ForEach-Object { [long]$_.timeStamp + [long]$_.elapsed } | Measure-Object -Maximum).Maximum
    $seconds = [Math]::Max(0.001, ($end - $begin) / 1000.0)
    [pscustomobject][ordered]@{
        operation = $name; samples = $times.Count; errors = $errors
        errorPercent = [Math]::Round(100.0 * $errors / $times.Count, 3)
        meanMs = [Math]::Round(($times | Measure-Object -Average).Average, 2)
        p95Ms = $times[[Math]::Ceiling(0.95 * $times.Count) - 1]
        p99Ms = $times[[Math]::Ceiling(0.99 * $times.Count) - 1]
        maxMs = $times[-1]; requestsPerSecond = [Math]::Round($times.Count / $seconds, 2)
    }
}
$metrics = @(Get-Metrics 'TOTAL' $measured)
foreach ($group in ($measured | Group-Object label)) { $metrics += Get-Metrics $group.Name $group.Group }
$peak = ($measured | ForEach-Object { [int]$_.allThreads } | Measure-Object -Maximum).Maximum
$passing = $peak -ge $ExpectedUsers -and ($metrics.Count - 1) -eq $ExpectedOperations
foreach ($metric in $metrics) {
    if ($metric.p95Ms -gt $P95LimitMs -or $metric.errorPercent -gt $ErrorLimitPercent) { $passing = $false }
}
New-Item -ItemType Directory -Path $OutputDirectory -Force | Out-Null
$metrics | Export-Csv -LiteralPath (Join-Path $OutputDirectory 'resumen.csv') -NoTypeInformation -Encoding UTF8
[ordered]@{
    passed = $passing; expectedUsers = $ExpectedUsers; peakActiveUsers = $peak
    expectedOperations = $ExpectedOperations; observedOperations = $metrics.Count - 1
    excludedInitialSeconds = $ExcludeInitialSeconds; excludedSamples = $rows.Count - $measured.Count
    measurementAnchor = 'Primera muestra HTTP registrada; excluye rampa y calentamiento desde ese instante'
    p95LimitMs = $P95LimitMs; errorLimitPercent = $ErrorLimitPercent; metrics = $metrics
    scope = 'Consultas GET autenticadas; exito de solicitudes no equivale a disponibilidad temporal'
} | ConvertTo-Json -Depth 8 | Set-Content -LiteralPath (Join-Path $OutputDirectory 'resumen.json') -Encoding UTF8
$metrics | Format-Table -AutoSize | Out-Host
if (-not $passing) { exit 1 }
exit 0
