@echo off
setlocal
set STEPLY_HOME=%~dp0\..
set JAVA_BIN=%STEPLY_HOME%\jre\bin\java.exe
set CLASSPATH=%STEPLY_HOME%\lib\*
"%JAVA_BIN%" -cp "%CLASSPATH%" -Dlogback.configurationFile="%STEPLY_HOME%\config\logback.xml" org.jsmart.steply.cli.SteplyCLI %*
endlocal