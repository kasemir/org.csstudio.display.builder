rem WINDOWS Command-line build of display.builder
rem provided by https://github.com/pavel-ch
rem
rem Results in P2
rem   repository/target/repository
rem from which "Display Builder" feature can
rem be installed into CS-Studio.
rem
rem Requires
rem JAVA_HOME - Java home
rem M2_HOME   - Maven 3.2.x
rem PATH      - Must include $M2_HOME/bin and $JAVA_HOME/bin"
rem CSS_REPO  - CS-Studio P2 repo
rem tee.exe   - utility installed and in the PATH (https://sourceforge.net/projects/unxutils/files/unxutils/current/UnxUtils.zip/download)

rem Set to the locally built repository or to a web repo
rem set CSS_REPO=file:%HOME%/git/org.csstudio.sns/repository/target/repository
set CSS_REPO=http://download.controlsystemstudio.org/updates/4.4

cd ..
rem mvn -version

rem Pick options...
rem set OPTIONS=--offline
set OPTIONS=-DskipTests -Dmaven.test.skip=true
mvn %OPTIONS% -Dcss-repo=%CSS_REPO% clean verify | tee build.log
