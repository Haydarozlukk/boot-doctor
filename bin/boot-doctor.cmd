@echo off
setlocal
set "SCRIPT_DIR=%~dp0"
set "BOOT_DOCTOR_JAR=%SCRIPT_DIR%..\target\boot-doctor.jar"

if not exist "%BOOT_DOCTOR_JAR%" (
    echo Boot Doctor JAR not found. Run "mvn package" first. 1>&2
    exit /b 1
)

java -jar "%BOOT_DOCTOR_JAR%" %*

