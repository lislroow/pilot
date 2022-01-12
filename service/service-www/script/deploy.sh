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


APP_NAME=service-www
PROFILE_SYS=$1
case ${PROFILE_SYS} in
  dev)
    APP_HOME=/app/WAS/pilot
    
    NX_URL="https://nexus/repository/maven-snapshot"
    XML_URL="${NX_URL}/mgkim/service/${APP_NAME}/maven-metadata.xml"
    
    app_version=$(curl -s ${XML_URL} | xmllint --xpath "//version[last()]/text()" -)
    echo "app_version=${app_version}"
    
    XML_URL="${NX_URL}/mgkim/service/${APP_NAME}/${app_version}/maven-metadata.xml"
    app_snap_version=$(curl -s ${XML_URL} | xmllint --xpath "//snapshotVersion[1]/value/text()" -)
    echo "app_snap_version=${app_snap_version}"
    
    JAR_URL="${NX_URL}/mgkim/service/${APP_NAME}/${app_version}/${APP_NAME}-${app_snap_version}.jar"
    JAR_FILE="${APP_NAME}-${app_snap_version}.jar"
    #JAR_URL="${NX_URL}/mgkim/service/service-www/2.0-SNAPSHOT/service-www-2.0-20220112.031025-3.jar"
    echo "JAR_URL=${JAR_URL}"
    curl --silent --output ${APP_HOME}/${JAR_FILE} ${JAR_URL}
    
    CHECKSUM=$(${MD5SUM_CMD} ${APP_HOME}/${JAR_FILE} | awk '{ print substr($1, 1, 4) }')
    JAR_MD5_FILE=${JAR_FILE%.*}_${CHECKSUM}.${JAR_FILE##*.}
    mv ${APP_HOME}/${JAR_FILE} ${APP_HOME}/${JAR_MD5_FILE}
    
    # result
    echo "${JAR_MD5_FILE}"
    
    ${APP_HOME}/stop.sh dwww11
    ${APP_HOME}/start.sh dwww11 ${JAR_MD5_FILE}
    
    ${APP_HOME}/stop.sh dwww12
    ${APP_HOME}/start.sh dwww12 ${JAR_MD5_FILE}
    ;;
  sta*)
    ;;
esac

