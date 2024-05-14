setlocal
cd /d %~dp0
mvn-caller.bat "mvn clean install -Dmaven.test.skip=true dependency:copy-dependencies -DoutputDirectory=target/lib"
endlocal