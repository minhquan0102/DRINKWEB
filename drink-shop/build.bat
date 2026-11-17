@echo off
chcp 65001 >nul
setlocal
rem Dong goi ung dung thanh file jar trong thu muc target
set "MAVEN=%~dp0..\tools\apache-maven-3.9.11\bin\mvn.cmd"
set "REPO=%~dp0..\tools\m2repo"

if not exist "%MAVEN%" (
    echo Khong tim thay Maven tai: %MAVEN%
    pause
    exit /b 1
)

call "%MAVEN%" -Dmaven.repo.local=%REPO% -DskipTests package
echo.
echo Xong. Chay bang lenh: java -jar target\drink-shop-1.0.0.jar
pause
endlocal
