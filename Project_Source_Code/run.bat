@echo off
title Library Management System
echo Compiling all Java files in this folder...
if not exist "bin" mkdir bin
javac -encoding UTF-8 -d bin *.java
if %errorlevel% neq 0 (
    echo [ERROR] Compilation failed.
    pause
    exit /b 1
)
copy /y db.properties bin\ >nul 2>&1
echo Running application...
java -cp "bin;lib/*" library.Main
pause
