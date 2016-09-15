#!/bin/sh
#
# Command-line build of display.builder
#
# Results in P2
#   repository/target/repository
# from which "Display Builder" feature can
# be installed into CS-Studio.
#
# Requires
# JAVA_HOME - Java home
# M2_HOME   - Maven 3.2.x
# PATH      - Must include $M2_HOME/bin and $JAVA_HOME/bin"
# CSS_REPO  - CS-Studio P2 repo
#
# For examples, see setup*sh file(s)

cd ..
mvn -version

# Pick options...
OPTIONS="--offline"
OPTIONS="-DskipTests -Dmaven.test.skip=true"
mvn $OPTIONS -Dcss-repo=$CSS_REPO clean verify | tee build.log

