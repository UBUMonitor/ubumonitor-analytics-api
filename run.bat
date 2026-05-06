@echo off
cd /d %~dp0

set PORT=9090
set JAR=

echo =====================================
echo   UBUMonitor Analytics
echo =====================================

REM ================================
REM 1. Liberar puerto 8080
REM ================================
echo Buscando procesos en puerto %PORT%...

for /f "tokens=5" %%a in ('netstat -ano ^| findstr :%PORT% ^| findstr LISTENING') do (
    echo Matando proceso con PID %%a
    taskkill /F /PID %%a >nul 2>&1
)

REM ================================
REM 2. Buscar JAR automaticamente
REM ================================
for %%f in (target\*.jar) do (
    set JAR=%%f
    goto :jar_found
)

:jar_found
if "%JAR%"=="" (
    echo.
    echo No se encontro ningun JAR en target\
    echo Ejecutando Maven build...

    mvn clean install

    if errorlevel 1 (
        echo ERROR: Fallo el build de Maven
        pause
        exit /b 1
    )

    for %%f in (target\*.jar) do (
        set JAR=%%f
        goto :jar_found_after_build
    )
)

:jar_found_after_build

echo.
echo Usando JAR: %JAR%

REM ================================
REM 3. Ejecutar Spring Boot (DEV)
REM ================================
echo.
echo Iniciando aplicacion Spring Boot en puerto %PORT% con perfil DEV...
echo -------------------------------------

java -Dspring.profiles.active=dev -jar "%JAR%"

echo.
echo Aplicacion detenida.
pause
