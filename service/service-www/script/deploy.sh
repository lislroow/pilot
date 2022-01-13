#!/bin/bash

echo "### [start] ${0##*/} ${@} ###"

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
      echo "metadata_url=${metadata_url}"
      app_ver=$(curl -s ${metadata_url} | xmllint --xpath "//version[last()]/text()" -)
      echo "app_ver=${app_ver}"
      
      metadata_url="${nexus_url}/mgkim/service/${APP_NAME}/${app_ver}/maven-metadata.xml"
      echo "metadata_url=${metadata_url}"
      app_snap_ver=$(curl -s ${metadata_url} | xmllint --xpath "//snapshotVersion[1]/value/text()" -)
      snap_timestamp=$(curl -s ${metadata_url} | xmllint --xpath "//timestamp/text()" -)
      snap_buildNumber=$(curl -s ${metadata_url} | xmllint --xpath "//buildNumber/text()" -)
      echo "app_snap_ver=${app_snap_ver}"
      echo "snap_timestamp=${snap_timestamp},snap_buildNumber=${snap_buildNumber}"
      
      DOWNLOAD_URL="${nexus_url}/mgkim/service/${APP_NAME}/${app_ver}/${APP_NAME}-${app_snap_ver}.jar"
      jar_file="${APP_NAME}-${app_ver}-${snap_timestamp}-${snap_buildNumber}.jar"
      echo "DOWNLOAD_URL=${DOWNLOAD_URL} to jar_file=${jar_file}"
      
      DOWNLOAD_CMD="curl --silent --output ${BASEDIR}/${jar_file} ${DOWNLOAD_URL}"
      echo "DOWNLOAD_CMD=${DOWNLOAD_CMD}"
      if [ $(whoami) == "root" ]; then
        su ${EXEC_USER} -c "${DOWNLOAD_CMD}"
      elif [ $(whoami) == ${EXEC_USER} ]; then
        eval "${DOWNLOAD_CMD}"
      else
        echo "current user "$(whoami)
        exit -1
      fi
      
      md5str=$(md5sum ${BASEDIR}/${jar_file} | awk '{ print substr($1, 1, 4) }')
      FINAL_NAME=${jar_file%.*}_${md5str}.${jar_file##*.}
      MV_CMD="mv ${BASEDIR}/${jar_file} ${BASEDIR}/${FINAL_NAME}"
      echo "MV_CMD=${MV_CMD}"
      if [ $(whoami) == "root" ]; then
        su ${EXEC_USER} -c "${MV_CMD}"
      elif [ $(whoami) == ${EXEC_USER} ]; then
        eval "${MV_CMD}"
      else
        echo "current user "$(whoami)
        exit -1
      fi
      
      # result
      echo "FINAL_NAME=${FINAL_NAME}"
      
      if [ $(whoami) == "root" ]; then
        su ${EXEC_USER} -c "${BASEDIR}/stop.sh dwww11"
        su ${EXEC_USER} -c "${BASEDIR}/start.sh dwww11 ${FINAL_NAME}"
        su ${EXEC_USER} -c "${BASEDIR}/stop.sh dwww12"
        su ${EXEC_USER} -c "${BASEDIR}/start.sh dwww12 ${FINAL_NAME}"
      elif [ $(whoami) == ${EXEC_USER} ]; then
        ${BASEDIR}/stop.sh dwww11
        ${BASEDIR}/start.sh dwww11 ${FINAL_NAME}
        ${BASEDIR}/stop.sh dwww12
        ${BASEDIR}/start.sh dwww12 ${FINAL_NAME}
      else
        echo "current user "$(whoami)
        exit -1
      fi
      ;;
    sta*)
      nexus_url="https://nexus/repository/maven-release"
      metadata_url="${nexus_url}/mgkim/service/${APP_NAME}/maven-metadata.xml"
      echo "metadata_url=${metadata_url}"
      app_ver=$(curl -s ${metadata_url} | xmllint --xpath "//version[last()]/text()" -)
      echo "app_ver=${app_ver}"
      
      DOWNLOAD_URL="${nexus_url}/mgkim/service/${APP_NAME}/${app_ver}/${APP_NAME}-${app_ver}.jar"
      jar_file="${APP_NAME}-${app_ver}.jar"
      echo "DOWNLOAD_URL=${DOWNLOAD_URL} to jar_file=${jar_file}"
      
      DOWNLOAD_CMD="curl --silent --output ${BASEDIR}/${jar_file} ${DOWNLOAD_URL}"
      echo "DOWNLOAD_CMD=${DOWNLOAD_CMD}"
      if [ $(whoami) == "root" ]; then
        su ${EXEC_USER} -c "${DOWNLOAD_CMD}"
      elif [ $(whoami) == ${EXEC_USER} ]; then
        eval "${DOWNLOAD_CMD}"
      else
        echo "current user "$(whoami)
        exit -1
      fi
      
      md5str=$(md5sum ${BASEDIR}/${jar_file} | awk '{ print substr($1, 1, 4) }')
      FINAL_NAME=${jar_file%.*}_${md5str}.${jar_file##*.}
      MV_CMD="mv ${BASEDIR}/${jar_file} ${BASEDIR}/${FINAL_NAME}"
      echo "MV_CMD=${MV_CMD}"
      if [ $(whoami) == "root" ]; then
        su ${EXEC_USER} -c "${MV_CMD}"
      elif [ $(whoami) == ${EXEC_USER} ]; then
        eval "${MV_CMD}"
      else
        echo "current user "$(whoami)
        exit -1
      fi
      
      # result
      echo "FINAL_NAME=${FINAL_NAME}"
      
      if [ $(whoami) == "root" ]; then
        su ${EXEC_USER} -c "${BASEDIR}/stop.sh swww11"
        su ${EXEC_USER} -c "${BASEDIR}/start.sh swww11 ${FINAL_NAME}"
        su ${EXEC_USER} -c "${BASEDIR}/stop.sh swww12"
        su ${EXEC_USER} -c "${BASEDIR}/start.sh swww12 ${FINAL_NAME}"
      elif [ $(whoami) == ${EXEC_USER} ]; then
        ${BASEDIR}/stop.sh swww11
        ${BASEDIR}/start.sh swww11 ${FINAL_NAME}
        ${BASEDIR}/stop.sh swww12
        ${BASEDIR}/start.sh swww12 ${FINAL_NAME}
      else
        echo "current user "$(whoami)
        exit -1
      fi
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

echo "### [finish] ${0##*/} ${@} ###"$'\n'
