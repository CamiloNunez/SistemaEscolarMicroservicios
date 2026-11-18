@echo off
rem Uso: ejecutar.cmd [smoke^|carga^|estres^|detener]
set "ESCENARIO=%~1"
if "%ESCENARIO%"=="" set "ESCENARIO=smoke"
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0Todo-En-Uno.ps1" -Scenario %ESCENARIO%
