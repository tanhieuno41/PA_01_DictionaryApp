@echo off
cd /d "%~dp0"
if not exist "dist\SlangDictionary.jar" (
    echo Chua co dist\SlangDictionary.jar - dang build...
    powershell -NoProfile -ExecutionPolicy Bypass -File build.ps1 -SkipTests
    if errorlevel 1 (
        echo Build that bai.
        pause
        exit /b 1
    )
)
java -jar dist\SlangDictionary.jar %*
