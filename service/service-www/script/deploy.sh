#!/bin/bash

echo $'\n'"### [execute] ${0##*/} ${@} ###"

## env
echo "+++ (system-env) +++"
SCRIPT_DIR="$( cd $( dirname "$0" ) && pwd -P)"
BASEDIR="${SCRIPT_DIR}"

UNAME=`uname -s`
if [[ "${UNAME}" = "Linux"* ]]; then
  OS_NAME="linux"
elif [[ "${UNAME}" = "CYGWIN"* || "${UNAME}" = "MINGW"* ]]; then
  OS_NAME="win"
fi


printf '%s\n' $(cat << EOF
SCRIPT_DIR=${SCRIPT_DIR}
BASEDIR=${BASEDIR}
UNAME=${UNAME}
OS_NAME=${OS_NAME}
EOF
)
echo "--- (system-env) ---"



## (deploy) deploy
function deploy() {
  echo "+++ (deploy) deploy +++"
  case ${PROFILE_SYS} in
    dev)
      nexus_url="https://nexus/repository/maven-snapshot"
      metadata_url="${nexus_url}/mgkim/service/${APP_NAME}/maven-metadata.xml"
      
      app_ver=$(curl -s ${metadata_url} | xmllint --xpath "//version[last()]/text()" -)
      echo "app_ver=${app_ver}"
      
      metadata_url="${nexus_url}/mgkim/service/${APP_NAME}/${app_ver}/maven-metadata.xml"
      app_snap_ver=$(curl -s ${metadata_url} | xmllint --xpath "//snapshotVersion[1]/value/text()" -)
      echo "app_snap_ver=${app_snap_ver}"
      
      DOWNLOAD_URL="${nexus_url}/mgkim/service/${APP_NAME}/${app_ver}/${APP_NAME}-${app_snap_ver}.jar"
      jar_file="${APP_NAME}-${app_snap_ver}.jar"
      echo "DOWNLOAD_URL=${DOWNLOAD_URL}"
      curl --silent --output ${BASEDIR}/${jar_file} ${DOWNLOAD_URL}
      
      md5str=$(md5sum ${BASEDIR}/${jar_file} | awk '{ print substr($1, 1, 4) }')
      FINAL_NAME=${jar_file%.*}_${md5str}.${jar_file##*.}
      mv ${BASEDIR}/${jar_file} ${BASEDIR}/${FINAL_NAME}
      
      # result
      echo "FINAL_NAME=${FINAL_NAME}"
      
      ${BASEDIR}/stop.sh dwww11
      ${BASEDIR}/start.sh dwww11 ${FINAL_NAME}
      
      ${BASEDIR}/stop.sh dwww12
      ${BASEDIR}/start.sh dwww12 ${FINAL_NAME}
      ;;
    sta*)
      exit -1
      ;;
    *)
      exit -1
      ;;
  esac
  echo "--- (deploy) deploy ---"
}

echo "+++ (runtime-env) +++"
EXEC_USER="tomcat"
APP_NAME="service-www"
PROFILE_SYS=$1

if [ "$1" == "" ]; then
  PROFILE_SYS="dev"
fi

printf '%s\n' $(cat << EOF
EXEC_USER=${EXEC_USER}
APP_NAME=${APP_NAME}
PROFILE_SYS=${PROFILE_SYS}
EOF
)
echo "--- (runtime-env) ---"


deploy;
