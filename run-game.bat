@echo off
setlocal

REM ====== CONFIG ======
set "PROJECT_DIR=C:\Users\profil\Desktop\jeux_JEE"
set "MAVEN_HOME=C:\Users\profil\Desktop\apache-maven-3.9.6"
set "TOMCAT_HOME=C:\Users\profil\Desktop\apache-tomcat-10.1.24"
set "JAVA_HOME=C:\Program Files\Java\jdk-17"

set "WAR_NAME=jeu-reflexion.war"
set "WAR_SOURCE=%PROJECT_DIR%\target\%WAR_NAME%"
set "WAR_TARGET=%TOMCAT_HOME%\webapps\ROOT.war"

echo.
echo ==========================================
echo  Memory Game - One Click Deploy
echo ==========================================
echo.

if not exist "%PROJECT_DIR%\pom.xml" (
  echo [ERROR] Projet introuvable: %PROJECT_DIR%
  exit /b 1
)

if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
  echo [ERROR] Maven introuvable: %MAVEN_HOME%
  exit /b 1
)

if not exist "%TOMCAT_HOME%\bin\startup.bat" (
  echo [ERROR] Tomcat introuvable: %TOMCAT_HOME%
  exit /b 1
)

if not exist "%JAVA_HOME%\bin\java.exe" (
  echo [ERROR] Java introuvable: %JAVA_HOME%
  exit /b 1
)

set "JRE_HOME=%JAVA_HOME%"
set "CATALINA_HOME=%TOMCAT_HOME%"
set "CATALINA_BASE=%TOMCAT_HOME%"

echo [1/5] Stop Tomcat...
powershell -NoProfile -ExecutionPolicy Bypass -Command "Get-CimInstance Win32_Process | Where-Object { $_.Name -eq 'java.exe' -and $_.CommandLine -like '*apache-tomcat-10.1.24*' } | ForEach-Object { Stop-Process -Id $_.ProcessId -Force }" >nul 2>&1
timeout /t 3 /nobreak >nul

echo [2/5] Build WAR with Maven...
cd /d "%PROJECT_DIR%"
call "%MAVEN_HOME%\bin\mvn.cmd" clean package -DskipTests
if errorlevel 1 (
  echo [ERROR] Build Maven echoue.
  exit /b 1
)

if not exist "%WAR_SOURCE%" (
  echo [ERROR] WAR non genere: %WAR_SOURCE%
  exit /b 1
)

echo [3/5] Clean previous ROOT deployment...
powershell -NoProfile -ExecutionPolicy Bypass -Command "Remove-Item -Recurse -Force '%TOMCAT_HOME%\webapps\ROOT' -ErrorAction SilentlyContinue; Remove-Item -Force '%TOMCAT_HOME%\webapps\ROOT.war' -ErrorAction SilentlyContinue" >nul 2>&1

echo [4/5] Deploy new WAR as ROOT...
copy /y "%WAR_SOURCE%" "%WAR_TARGET%" >nul
if errorlevel 1 (
  echo [ERROR] Copie du WAR vers Tomcat echouee.
  exit /b 1
)

echo [5/5] Start Tomcat...
call "%TOMCAT_HOME%\bin\startup.bat"

echo.
echo ==========================================
echo  DONE - Ouvre: http://localhost:8080/login
echo ==========================================
echo.

endlocal
exit /b 0
