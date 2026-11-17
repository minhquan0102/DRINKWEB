@echo off
chcp 65001 >nul
setlocal
rem Chay ung dung bang Maven di kem trong D:\AMLO\tools (khong can cai Maven)
set "MAVEN=%~dp0..\tools\apache-maven-3.9.11\bin\mvn.cmd"
set "REPO=%~dp0..\tools\m2repo"

if not exist "%MAVEN%" (
    echo Khong tim thay Maven tai: %MAVEN%
    echo Hay mo project bang IDE (IntelliJ/Eclipse) va chay DrinkShopApplication.
    pause
    exit /b 1
)

echo Dang khoi dong Drink Shop tren http://localhost:8080 ...
call "%MAVEN%" -Dmaven.repo.local=%REPO% spring-boot:run
endlocal
