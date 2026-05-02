@echo off
setlocal enabledelayedexpansion

REM ====== AUTO-DETECT PATHS ======

REM Project dir = folder containing this .bat file
set "PROJECT_DIR=%~dp0"
if "%PROJECT_DIR:~-1%"=="\" set "PROJECT_DIR=%PROJECT_DIR:~0,-1%"

REM --- JAVA_HOME (prefer Java 17+, use for /d to expand wildcards) ---
if not defined JAVA_HOME (
  REM Search Eclipse Adoptium for JDK 17 or 21
  for /d %%p in (
    "C:\Program Files\Eclipse Adoptium\jdk-21*"
    "C:\Program Files\Eclipse Adoptium\jdk-17*"
    "C:\Program Files\Microsoft\jdk-17*"
    "C:\Program Files\Microsoft\jdk-21*"
    "C:\Program Files\Java\jdk-17*"
    "C:\Program Files\Java\jdk-21*"
  ) do (
    if exist "%%p\bin\java.exe" (
      set "JAVA_HOME=%%p"
      goto :java_found
    )
  )
  REM Fallback: use whatever java is in PATH
  for /f "tokens=*" %%i in ('where java 2^>nul') do (
    set "JAVA_HOME=%%~dpi.."
    goto :java_found
  )
  echo [ERROR] Java introuvable. Installe JDK 17+ et ajoute-le au PATH.
  pause & exit /b 1
)
:java_found

REM --- MAVEN_HOME ---
if not defined MAVEN_HOME (
  REM Check if mvn is in PATH
  for /f "tokens=*" %%i in ('where mvn 2^>nul') do (
    set "MAVEN_HOME=%%~dpi.."
    goto :maven_found
  )
  for /f "tokens=*" %%i in ('where mvn.cmd 2^>nul') do (
    set "MAVEN_HOME=%%~dpi.."
    goto :maven_found
  )
  REM Try common install locations
  for /d %%p in (
    "C:\Program Files\apache-maven*"
    "C:\Program Files\apache-maven-3.9*\apache-maven-3.9*"
    "%USERPROFILE%\Desktop\apache-maven*"
    "C:\tools\maven"
    "C:\maven"
  ) do (
    if exist "%%p\bin\mvn.cmd" (
      set "MAVEN_HOME=%%p"
      goto :maven_found
    )
  )
  echo [ERROR] Maven introuvable. Installe Apache Maven et ajoute-le au PATH.
  pause & exit /b 1
)
:maven_found

REM --- TOMCAT_HOME ---
if not defined TOMCAT_HOME (
  for /d %%p in (
    "%USERPROFILE%\Desktop\apache-tomcat-10*"
    "C:\Program Files\Apache Software Foundation\Tomcat 10*"
    "%USERPROFILE%\apache-tomcat-10*"
    "C:\tools\tomcat"
    "C:\tomcat"
  ) do (
    if exist "%%p\bin\startup.bat" (
      set "TOMCAT_HOME=%%p"
      goto :tomcat_found
    )
  )
  echo [ERROR] Tomcat introuvable.
  echo.
  set /p "TOMCAT_HOME=Entre le chemin vers Tomcat (ex: C:\tomcat): "
  if not exist "!TOMCAT_HOME!\bin\startup.bat" (
    echo [ERROR] Chemin Tomcat invalide: !TOMCAT_HOME!
    pause & exit /b 1
  )
)
:tomcat_found

REM ====== VALIDATE ======
set "WAR_NAME=jeu-reflexion.war"
set "WAR_SOURCE=%PROJECT_DIR%\target\%WAR_NAME%"
set "WAR_TARGET=%TOMCAT_HOME%\webapps\ROOT.war"

echo.
echo ==========================================
echo  Memory Game - One Click Deploy
echo ==========================================
echo  Project : %PROJECT_DIR%
echo  Java    : %JAVA_HOME%
echo  Maven   : %MAVEN_HOME%
echo  Tomcat  : %TOMCAT_HOME%
echo ==========================================
echo.

if not exist "%PROJECT_DIR%\pom.xml" (
  echo [ERROR] pom.xml introuvable dans: %PROJECT_DIR%
  pause & exit /b 1
)
if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
  echo [ERROR] mvn.cmd introuvable dans: %MAVEN_HOME%\bin
  pause & exit /b 1
)
if not exist "%TOMCAT_HOME%\bin\startup.bat" (
  echo [ERROR] startup.bat introuvable dans: %TOMCAT_HOME%\bin
  pause & exit /b 1
)
if not exist "%JAVA_HOME%\bin\java.exe" (
  echo [ERROR] java.exe introuvable dans: %JAVA_HOME%\bin
  pause & exit /b 1
)

set "JRE_HOME=%JAVA_HOME%"
set "CATALINA_HOME=%TOMCAT_HOME%"
set "CATALINA_BASE=%TOMCAT_HOME%"
set "PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%"

echo [1/5] Stop Tomcat (si actif)...
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
  "Get-CimInstance Win32_Process | Where-Object { $_.Name -eq 'java.exe' -and $_.CommandLine -like '*catalina*' } | ForEach-Object { Stop-Process -Id $_.ProcessId -Force }" >nul 2>&1
timeout /t 3 /nobreak >nul

echo [2/5] Build WAR avec Maven...
cd /d "%PROJECT_DIR%"
call "%MAVEN_HOME%\bin\mvn.cmd" clean package -DskipTests
if errorlevel 1 (
  echo [ERROR] Build Maven echoue.
  pause & exit /b 1
)

if not exist "%WAR_SOURCE%" (
  echo [ERROR] WAR non genere: %WAR_SOURCE%
  pause & exit /b 1
)

echo [3/5] Suppression du deploiement ROOT precedent...
if exist "%TOMCAT_HOME%\webapps\ROOT" (
  rd /s /q "%TOMCAT_HOME%\webapps\ROOT" >nul 2>&1
)
if exist "%TOMCAT_HOME%\webapps\ROOT.war" (
  del /f /q "%TOMCAT_HOME%\webapps\ROOT.war" >nul 2>&1
)

echo [4/5] Deploiement du nouveau WAR...
copy /y "%WAR_SOURCE%" "%WAR_TARGET%" >nul
if errorlevel 1 (
  echo [ERROR] Copie du WAR vers Tomcat echouee.
  pause & exit /b 1
)

echo [5/5] Demarrage de Tomcat...
call "%TOMCAT_HOME%\bin\startup.bat"

echo.
echo ==========================================
echo  DONE - Ouvre: http://localhost:8080/login
echo ==========================================
echo.

endlocal
exit /b 0
