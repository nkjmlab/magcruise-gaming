setlocal
cd /d %~dp0
mvn-caller.bat "mvn clean install -Dmaven.test.skip=true dependency:copy-dependencies -DoutputDirectory=target/lib" "magcruise-broker\target\classes\broker.bat" "magcruise-webui\target\classes\webui.bat" "timeout 5" "mvn test -DexcludedGroups=public_server" "jps -lm|grep org.magcruise. | gawk "{print $1}" | xargs -r -n1 taskkill /F /T /PID"
endlocal
