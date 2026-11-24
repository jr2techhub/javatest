#!/bin/sh
basedir="$(cd "$(dirname "$0")"; pwd)"
WRAPPER_JAR="$basedir/.mvn/wrapper/maven-wrapper.jar"
WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain
exec "${JAVA_HOME:+$JAVA_HOME/bin/}java" ${MAVEN_OPTS} -classpath "$WRAPPER_JAR" "-Dmaven.multiModuleProjectDirectory=$basedir" $WRAPPER_LAUNCHER "$@"
