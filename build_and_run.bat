@echo off
title Network Flow Optimization System (DSA-3)
echo ===================================================================
echo   Compiling and Launching Network Flow Optimization System (Java)
echo ===================================================================

cd /d "%~dp0"
if not exist "bin" mkdir bin

echo Compiling Java source files...
javac -encoding UTF-8 -d bin src/model/*.java src/algorithms/*.java src/data/*.java src/benchmark/*.java src/ui/*.java src/Main.java test/*.java

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed. Please check your Java installation.
    pause
    exit /b %ERRORLEVEL%
)

echo Compilation successful! Launching interactive application...
echo.
java -cp bin Main
pause
