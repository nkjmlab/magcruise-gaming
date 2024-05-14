setlocal
cd /d %~dp0
mvn-caller.bat "magcruise-broker\target\classes\broker.bat" "magcruise-webui\target\classes\webui.bat" "timeout 5" "mvn test" "jps -lm|grep org.magcruise. | gawk "{print $1}" | xargs -r -n1 taskkill /F /T /PID"
endlocal
