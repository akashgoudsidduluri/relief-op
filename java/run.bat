@echo off
echo Compiling the Java Project...
cd src
javac -cp ".;..\lib\mysql-connector-j-9.6.0.jar" *.java panels\*.java
if %ERRORLEVEL% GEQ 1 (
    echo Compilation Failed!
    pause
    exit /b %ERRORLEVEL%
)
echo Compilation Successful! Booting up Relief-OP...
echo.
java -cp ".;..\lib\mysql-connector-j-9.6.0.jar" Main
pause
