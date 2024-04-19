setlocal
chcp 65001
@echo off
set CURRENT_DIR=%~dp0
cd /d %CURRENT_DIR%
cd ../
jps -lm|grep org.magcruise. | gawk "{print $1}" | xargs -r -n1 taskkill /F /T /PID

call mvn clean install -Dmaven.test.skip=true dependency:copy-dependencies -DoutputDirectory=target/lib

@if errorlevel 1 pause
endlocal
