@echo off
chcp 65001 >nul
setlocal
set "PORT=8080"
if not "%~1"=="" set "PORT=%~1"

echo Dang tim tien trinh giu cong %PORT% ...
set "FOUND="
for /f "tokens=5" %%p in ('netstat -ano ^| findstr ":%PORT%" ^| findstr "LISTENING"') do (
    set "FOUND=1"
    echo  - Tat PID %%p
    taskkill /PID %%p /F >nul 2>&1
)

if not defined FOUND (
    echo Khong co tien trinh nao dang giu cong %PORT%.
) else (
    echo Da giai phong cong %PORT%.
)
pause
endlocal
