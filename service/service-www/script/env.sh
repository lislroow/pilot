#!/bin/bash

UNAME=`uname -s`
#echo "UNAME=${UNAME}"
if [[ "${UNAME}" = "Linux"* ]]; then
  OS_NAME="linux"
elif [[ "${UNAME}" = "CYGWIN"* || "${UNAME}" = "MINGW"* ]]; then
  OS_NAME="win"
fi

#echo "OS_NAME=${OS_NAME}"

case ${OS_NAME} in
  linux)
    M2_HOME=/prod/maven/maven
    JAVA_HOME=/prod/java/openjdk-11.0.13.8-temurin
    PATH=$JAVA_HOME/bin:$M2_HOME/bin:$PATH
    MD5SUM_CMD=md5sum
    DIRNAME_CMD=dirname
    ;;
  win)
    M2_HOME=/z/develop/build/maven-3.6.3
    JAVA_HOME=/z/develop/java/openjdk-11.0.13.8-temurin
    PATH=$JAVA_HOME/bin:$M2_HOME/bin:$PATH
    MD5SUM_CMD=md5sum.exe
    DIRNAME_CMD=dirname.exe
    ;;
  *)
    echo "invalid os"
    exit -1
    ;;
esac

#PROJECT_BASE="$( cd $( ${DIRNAME_CMD} "$0" )/.. && pwd -P)"
#echo "${PROJECT_BASE}"
