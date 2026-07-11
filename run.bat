@echo off
cd /d %~dp0

set PORT=9090
set JAR=

echo =====================================
echo   UBUMonitor Analytics
echo =====================================

REM ================================
REM  LIBERAR PUERTO APP
REM ================================
echo Buscando procesos en puerto %PORT%...

for /f "tokens=5" %%a in ('netstat -ano ^| findstr :%PORT% ^| findstr LISTENING') do (
    echo Matando proceso con PID %%a
    taskkill /F /PID %%a >nul 2>&1
)

REM ================================
REM  SIEMPRE COMPILAR MAVEN
REM ================================
echo.
echo Ejecutando Maven build SIEMPRE...
mvn clean install

if errorlevel 1 (
    echo ERROR: Fallo el build de Maven
    pause
    exit /b 1
)

REM ================================
REM  BUSCAR JAR AUTOMATICAMENTE
REM ================================
for %%f in (target\*.jar) do (
    set JAR=%%f
    goto :jar_found
)

:jar_found
if "%JAR%"=="" (
    echo ERROR: No se encontro ningun JAR en target\
    pause
    exit /b 1
)

echo.
echo Usando JAR: %JAR%

REM ================================
REM  EJECUTAR SPRING BOOT
REM ================================
echo.
echo =====================================
echo INICIANDO SPRING BOOT
echo =====================================
echo.

start "UBUMonitor Spring Boot" cmd /k java -Dspring.profiles.active=dev -jar "%JAR%"

echo.
echo =====================================
echo SPRING BOOT LANZADO EN OTRA VENTANA
echo =====================================
echo.

REM ================================
REM  MOSTRAR BDs H2
REM ================================
echo.
echo =====================================
echo BASES DE DATOS H2 ENCONTRADAS
echo =====================================
echo.

for /r %%f in (*.mv.db) do (

    set "FULL=%%f"
    call set "DB=%%FULL:.mv.db=%%"

    echo -------------------------------------
    echo Archivo:
    echo %%f
    echo.
    echo URL DBeaver:
    call echo jdbc:h2:tcp://localhost:9092/%%DB%%;CIPHER=AES;DATABASE_TO_UPPER=false;
    echo.
    echo -------------------------------------
)

echo IMPORTANTE: si hay cambios en codigo se ejecuta siempre mvn clean install automaticamente
pause
