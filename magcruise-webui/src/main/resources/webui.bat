@echo off
setlocal
cd /d %~dp0
cd ../
start /B javaw -cp classes;lib/* -Dfile.encoding=UTF-8 org.magcruise.gaming.webui.WebUiApplication
endlocal

