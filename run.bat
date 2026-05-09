@echo off
cd /d %~dp0

set PORT=9090
set JAR=

echo =====================================
echo   UBUMonitor Analytics
echo =====================================

REM ================================
REM  Liberar puerto APP
REM ================================
echo Buscando procesos en puerto %PORT%...

for /f "tokens=5" %%a in ('netstat -ano ^| findstr :%PORT% ^| findstr LISTENING') do (
    echo Matando proceso con PID %%a
    taskkill /F /PID %%a >nul 2>&1
)



REM ================================
REM  Buscar JAR automaticamente
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
REM  Ejecutar Spring Boot (DEV)
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
echo Las URLs de DBeaver quedan visibles aqui.
echo.


REM ================================
REM  Mostrar BDs H2 disponibles
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
    call echo jdbc:h2:tcp://localhost:9092/%%DB%%;CIPHER=AES
    echo.
    echo -------------------------------------

)
echo IMPORTANTE Por como funciona de carga bases de datos en tiempo de ejecucion se debe ejecutar primero alguna peticion a la API que use la BBDD antes de conectar en DBeaver
echo Si hay cambios en codigo se debe ejecutar primero el mvn clean o borrar la carpeta target
pause
