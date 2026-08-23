@echo off
cd /d "%~dp0backend"

if exist ".env" (
    echo Loading environment variables from backend\.env ...
    for /f "usebackq tokens=1* delims==" %%A in (`findstr /r /v "^[[:space:]]*#" .env`) do (
        if not "%%A"=="" set "%%A=%%B"
    )
)

if not defined JAVA_HOME (
    if exist "C:\Program Files\Java\jdk-21" (
        set "JAVA_HOME=C:\Program Files\Java\jdk-21"
    )
)
if defined JAVA_HOME (
    set "PATH=%JAVA_HOME%\bin;%PATH%"
)
echo Starting Salesforce CRUD Backend on port 8080...
call mvnw.cmd spring-boot:run
pause
