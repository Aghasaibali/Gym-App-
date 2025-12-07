@echo off
setlocal
echo === Gym Desktop App (MySQL-backed) ===
echo.

if not exist db-init.sql (
    echo [ERROR] db-init.sql not found. Cannot initialize database. Aborting.
    goto :end
)

set "MYSQL_CMD=mysql"
where %MYSQL_CMD% >NUL 2>&1
if errorlevel 1 (
    set "MYSQL_CMD="
    for %%p in ("C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe" "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" "C:\Program Files (x86)\MySQL\MySQL Server 8.0\bin\mysql.exe" "C:\xampp\mysql\bin\mysql.exe" "C:\wamp64\bin\mysql\mysql8.0.30\bin\mysql.exe" "C:\wamp64\bin\mysql\mysql8.0.28\bin\mysql.exe") do (
        if exist %%~p (
            set "MYSQL_CMD=%%~p"
            goto :foundmysql
        )
    )
    for /f "delims=" %%p in ('dir /b /s "C:\Program Files\MySQL\*\bin\mysql.exe" 2^>nul') do (
        set "MYSQL_CMD=%%~p"
        goto :foundmysql
    )
    echo [ERROR] mysql client not found. Add MySQL bin to PATH or edit run.bat to point to mysql.exe.
    goto :end
)

:foundmysql
echo [INFO] Using MySQL client: %MYSQL_CMD%

set "ROOTPASS=%DB_ROOT_PASS%"
if "%ROOTPASS%"=="" (
    set /p ROOTPASS=Enter MySQL root password (leave blank if none): 
)

echo [INFO] Initializing database from db-init.sql ...
if "%ROOTPASS%"=="" (
    "%MYSQL_CMD%" -u root < db-init.sql
) else (
    "%MYSQL_CMD%" -u root -p%ROOTPASS% < db-init.sql
)
if errorlevel 1 (
    echo [ERROR] Failed to initialize database. Check root password, MySQL service status, and mysql path.
    goto :end
)

echo [INFO] Starting app with Maven ...
call mvn -q javafx:run
if errorlevel 1 (
    echo [ERROR] Maven/javafx run failed. Ensure Maven is installed and JAVA_HOME is set.
    goto :end
)

:end
echo.
pause
