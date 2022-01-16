#!/bin/bash

echo "### [start] ${0##*/} ${@} ###"

## env
echo "+++ (system-env) +++"
BASEDIR="$( cd $( dirname "$0" ) && pwd -P)"

UNAME=`uname -s`
if [[ "${UNAME}" = "Linux"* ]]; then
  OS_NAME="linux"
elif [[ "${UNAME}" = "CYGWIN"* || "${UNAME}" = "MINGW"* ]]; then
  OS_NAME="win"
fi

printf '%s\n' $(cat << EOF
BASEDIR=${BASEDIR}
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
      local nexus_url="https://nexus/repository/maven-snapshot"
      local metadata_url="${nexus_url}/mgkim/service/${APP_NAME}/maven-metadata.xml"
      echo "metadata_url=${metadata_url}"
      local app_ver=$(curl -s ${metadata_url} | xmllint --xpath "//version[last()]/text()" -)
      echo "app_ver=${app_ver}"
      
      local metadata_url="${nexus_url}/mgkim/service/${APP_NAME}/${app_ver}/maven-metadata.xml"
      echo "metadata_url=${metadata_url}"
      local app_snap_ver=$(curl -s ${metadata_url} | xmllint --xpath "//snapshotVersion[1]/value/text()" -)
      local snap_timestamp=$(curl -s ${metadata_url} | xmllint --xpath "//timestamp/text()" -)
      local snap_buildNumber=$(curl -s ${metadata_url} | xmllint --xpath "//buildNumber/text()" -)
      echo "app_snap_ver=${app_snap_ver}"
      echo "snap_timestamp=${snap_timestamp},snap_buildNumber=${snap_buildNumber}"
      
      local download_url="${nexus_url}/mgkim/service/${APP_NAME}/${app_ver}/${APP_NAME}-${app_snap_ver}.jar"
      jar_file="${APP_NAME}-${app_ver}-${snap_timestamp}-${snap_buildNumber}.jar"
      echo "download_url=${download_url} to jar_file=${jar_file}"
      ;;
    sta)
      # lastest artifact (maven-release)
      local nexus_url="https://nexus/repository/maven-release"
      metadata_url="${nexus_url}/mgkim/service/${APP_NAME}/maven-metadata.xml"
      echo "metadata_url=${metadata_url}"
      app_ver=$(curl -s ${metadata_url} | xmllint --xpath "//version[last()]/text()" -)
      echo "app_ver=${app_ver}"
      
      local download_url="${nexus_url}/mgkim/service/${APP_NAME}/${app_ver}/${APP_NAME}-${app_ver}.jar"
      jar_file="${APP_NAME}-${app_ver}.jar"
      echo "download_url=${download_url} to jar_file=${jar_file}"
      ;;
    *)
      exit -1
      ;;
  esac
  
  # DOWNLOAD
  download_cmd="curl --silent --output ${BASEDIR}/${jar_file} ${download_url}"
  echo "download_cmd=${download_cmd}"
  if [ $(whoami) == "root" ]; then
    su ${EXEC_USER} -c "${download_cmd}"
  elif [ $(whoami) == ${EXEC_USER} ]; then
    eval "${download_cmd}"
  else
    echo "current user "$(whoami)
    exit -1
  fi
  
  # final_name
  local md5str=$(md5sum ${BASEDIR}/${jar_file} | awk '{ print substr($1, 1, 4) }')
  local final_name=${jar_file%.*}_${md5str}.${jar_file##*.}
  local mv_cmd="mv ${BASEDIR}/${jar_file} ${BASEDIR}/${final_name}"
  echo "mv_cmd=${mv_cmd}"
  if [ $(whoami) == "root" ]; then
    su ${EXEC_USER} -c "${mv_cmd}"
  elif [ $(whoami) == ${EXEC_USER} ]; then
    eval "${mv_cmd}"
  else
    echo "current user "$(whoami)
    exit -1
  fi
  echo "final_name=${final_name}"
  
  # stop / start
  for app_id in ${APP_ID_LIST[@]}
  do
    if [ $(whoami) == "root" ]; then
      su ${EXEC_USER} -c "${BASEDIR}/stop.sh ${app_id}"
      su ${EXEC_USER} -c "${BASEDIR}/start.sh ${app_id} ${final_name}"
    elif [ $(whoami) == ${EXEC_USER} ]; then
      ${BASEDIR}/stop.sh ${app_id}
      ${BASEDIR}/start.sh ${app_id} ${final_name}
    else
      echo "current user "$(whoami)
      exit -1
    fi
  done
  
  echo "--- //(deploy) deploy ---"
}

echo "+++ (runtime-env) +++"
EXEC_USER="tomcat"
PROFILE_SYS=$1
APP_NAME=$2
case "${PROFILE_SYS}:${APP_NAME}" in
  dev:*w*)
    APP_NAME="pilot-www"
    APP_ID_LIST=("dwww11" "dwww12")
    ;;
  sta:*w*)
    APP_NAME="pilot-www"
    APP_ID_LIST=("swww11" "swww12")
    ;;
  dev:*a*)
    APP_NAME="pilot-adm"
    APP_ID_LIST=("dadm11" "dadm12")
    ;;
  sta:*a*)
    APP_NAME="pilot-adm"
    APP_ID_LIST=("sadm11" "sadm12")
    ;;
  *)
    echo "Usage: ${0##*/} [dev|sta] [w|a]"
    exit -1
    ;;
esac

printf '%s\n' $(cat << EOF
EXEC_USER=${EXEC_USER}
PROFILE_SYS=${PROFILE_SYS}
APP_NAME=${APP_NAME}
APP_ID_LIST=${APP_ID_LIST}
EOF
)



deploy;

echo "### [finish] ${0##*/} ${@} ###"$'\n'$'\n'
