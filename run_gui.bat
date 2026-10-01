@echo off
title Network Flow Visualizer (Swing GUI)
echo Launching Network Flow Visualizer GUI...

cd /d "%~dp0"
if not exist "bin" mkdir bin

if not exist "bin\Main.class" (
    echo Compiling Java source files...
    javac -encoding UTF-8 -d bin src/model/*.java src/algorithms/*.java src/data/*.java src/benchmark/*.java src/ui/*.java src/Main.java test/*.java
)

java -cp bin Main --gui
