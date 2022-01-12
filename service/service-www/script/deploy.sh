#!/bin/bash

## env
. ./env.sh

APP_NAME=service-www
PROFILE_SYS=$1
case ${PROFILE_SYS} in
  dev)
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
    curl --silent --output ${JAR_FILE} ${JAR_URL}
    
    CHECKSUM=$(${MD5SUM_CMD} ${JAR_FILE} | awk '{ print substr($1, 1, 4) }')
    JAR_MD5_FILE=${JAR_FILE%.*}_${CHECKSUM}.${JAR_FILE##*.}
    mv ${JAR_FILE} ${JAR_MD5_FILE}
    ls -alh ${JAR_MD5_FILE}
    
    # result
    echo "${JAR_MD5_FILE}"
    
    ./stop.sh dwww11
    ./start.sh dwww11 ${JAR_MD5_FILE}
    
    ./stop.sh dwww12
    ./start.sh dwww12 ${JAR_MD5_FILE}
    ;;
  sta*)
    ;;
esac

