#!/bin/bash

echo $'\n'"### (file) ${0##*/} ${@} ###"

## env
echo $"+++ (system-env) +++"
SCRIPT_DIR="$( cd $( dirname "$0" ) && pwd -P)"
BASEDIR="${SCRIPT_DIR}"

printf '%s\n' $(cat << EOF
SCRIPT_DIR=${SCRIPT_DIR}
BASEDIR=${BASEDIR}
EOF
)
echo "--- (system-env) ---"

## (backup) backup except for lastest jar
function backup() {
  echo "+++ (backup) backup except for lastest jar +++"
  if [ ! -e ${ARCHIVE_DIR} ]; then
    mkdir -p ${ARCHIVE_DIR}
  fi
  
  LATEST_CMD="ls -rt ${BASEDIR}/*.jar | tail -n 1"
  #echo ${LATEST_CMD}
  LATEST_JAR=$(eval ${LATEST_CMD})
  
  OLD_CMD="find ${BASEDIR} -maxdepth 1 -type f ! -newer ${LATEST_JAR} -name '*.jar' ! -samefile ${LATEST_JAR}"
  #echo ${OLD_CMD}
  OLD_JAR=$(eval ${OLD_CMD})
  
  echo "LATEST_JAR=${LATEST_JAR}"
  echo "OLD_JAR=${OLD_JAR[*]}"
  
  if [ "${OLD_JAR[*]}" != "" ]; then
    MV_CMD="mv ${OLD_JAR[*]} ${ARCHIVE_DIR}"
    if [ $(whoami) == "root" ]; then
      su ${EXEC_USER} -c "${MV_CMD}"
    elif [ $(whoami) == ${EXEC_USER} ]; then
      eval "${MV_CMD}"
    else
      echo "current user "$(whoami)
      exit -1
    fi
  fi
  echo "--- (backup) backup except for lastest jar ---"
}


echo "+++ (runtime-env) +++"
EXEC_USER="tomcat"
ARCHIVE_DIR="${BASEDIR}/archive"

printf '%s\n' $(cat << EOF
EXEC_USER=${EXEC_USER}
ARCHIVE_DIR=${ARCHIVE_DIR}
EOF
)
echo "--- (runtime-env) ---"

backup;
