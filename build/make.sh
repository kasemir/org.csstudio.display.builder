#!/bin/sh
#
# Requires settings shown in the setup*sh file(s)

cd ..
mvn -version
mvn --offline -Dcss-repo=$CSS_REPO clean verify | tee build.log

