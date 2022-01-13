#!/bin/bash

echo "### [start] ${0##*/} ${@} ###"

## env
echo "+++ (system-env) +++"
SCRIPT_DIR="$( cd $( dirname "$0" ) && pwd -P)"

UNAME=`uname -s`
if [[ "${UNAME}" = "Linux"* ]]; then
  OS_NAME="linux"
elif [[ "${UNAME}" = "CYGWIN"* || "${UNAME}" = "MINGW"* ]]; then
  OS_NAME="win"
fi

printf '%s\n' $(cat << EOF
SCRIPT_DIR=${SCRIPT_DIR}
UNAME=${UNAME}
OS_NAME=${OS_NAME}
EOF
)


## (deploy) deploy
function deploy() {
  echo "+++ (deploy) deploy +++"
  case ${PROFILE_SYS} in
    dev)
      # lastest artifact (maven-snapshot)
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
      ;;
    sta)
      # lastest artifact (maven-release)
      nexus_url="https://nexus/repository/maven-release"
      metadata_url="${nexus_url}/mgkim/service/${APP_NAME}/maven-metadata.xml"
      echo "metadata_url=${metadata_url}"
      app_ver=$(curl -s ${metadata_url} | xmllint --xpath "//version[last()]/text()" -)
      echo "app_ver=${app_ver}"
      
      DOWNLOAD_URL="${nexus_url}/mgkim/service/${APP_NAME}/${app_ver}/${APP_NAME}-${app_ver}.jar"
      jar_file="${APP_NAME}-${app_ver}.jar"
      echo "DOWNLOAD_URL=${DOWNLOAD_URL} to jar_file=${jar_file}"
      ;;
    *)
      exit -1
      ;;
  esac
  
  # DOWNLOAD
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
  
  # FINAL_NAME
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
  echo "FINAL_NAME=${FINAL_NAME}"
  
  # stop / start
  for APP_ID in ${APP_ID_LIST[*]}
  do
    if [ $(whoami) == "root" ]; then
      su ${EXEC_USER} -c "${BASEDIR}/stop.sh ${APP_ID}"
      su ${EXEC_USER} -c "${BASEDIR}/start.sh ${APP_ID} ${FINAL_NAME}"
    elif [ $(whoami) == ${EXEC_USER} ]; then
      ${BASEDIR}/stop.sh ${APP_ID}
      ${BASEDIR}/start.sh ${APP_ID} ${FINAL_NAME}
    else
      echo "current user "$(whoami)
      exit -1
    fi
  done
  
  echo "--- //(deploy) deploy ---"
}

echo "+++ (runtime-env) +++"
EXEC_USER="tomcat"
BASEDIR="$( cd ${SCRIPT_DIR} && pwd -P)"
PROFILE_SYS=$1
APP_NAME=$2
case ${PROFILE_SYS}:${APP_NAME} in
  dev:*w*)
    APP_NAME="service-www"
    APP_ID_LIST=("dwww11" "dwww12")
    ;;
  sta:*w*)
    APP_NAME="service-www"
    APP_ID_LIST=("swww11" "swww12")
    ;;
  dev:*a*)
    APP_NAME="service-adm"
    APP_ID_LIST=("dadm11" "dadm12")
    ;;
  sta:*a*)
    APP_NAME="service-adm"
    APP_ID_LIST=("sadm11" "sadm12")
    ;;
  *)
    exit -1
    ;;
esac

printf '%s\n' $(cat << EOF
EXEC_USER=${EXEC_USER}
BASEDIR=${BASEDIR}
PROFILE_SYS=${PROFILE_SYS}
APP_NAME=${APP_NAME}
APP_ID_LIST=${APP_ID_LIST}
EOF
)



deploy;

echo "### [finish] ${0##*/} ${@} ###"$'\n'$'\n'
