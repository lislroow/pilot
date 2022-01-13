#!/bin/bash

echo "### [start] ${0##*/} ${@} ###"

## env
echo $"+++ (system-env) +++"
SCRIPT_DIR="$( cd $( dirname "$0" ) && pwd -P)"
BASEDIR="${SCRIPT_DIR}"

printf '%s\n' $(cat << EOF
SCRIPT_DIR=${SCRIPT_DIR}
BASEDIR=${BASEDIR}
EOF
)


## (backup) backup except for lastest jar
function backup() {
  echo "+++ (backup) backup except for lastest jar +++"
  if [ ! -e ${ARCHIVE_DIR} ]; then
    MKDIR_CMD="mkdir -p ${ARCHIVE_DIR}"
    if [ $(whoami) == "root" ]; then
      su ${EXEC_USER} -c "${MKDIR_CMD}"
    elif [ $(whoami) == ${EXEC_USER} ]; then
      eval "${MKDIR_CMD}"
    else
      echo "current user "$(whoami)
      exit -1
    fi
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
    MV_CMD="mv $(echo ${OLD_JAR[*]} | tr -d '\n') ${ARCHIVE_DIR}"
    echo "MV_CMD=${MV_CMD}"
    if [ $(whoami) == "root" ]; then
      su ${EXEC_USER} -c "${MV_CMD}"
    elif [ $(whoami) == ${EXEC_USER} ]; then
      eval "${MV_CMD}"
    else
      echo "current user "$(whoami)
      exit -1
    fi
  fi
  echo "--- //(backup) backup except for lastest jar ---"
}


echo "+++ (runtime-env) +++"
EXEC_USER="tomcat"
ARCHIVE_DIR="${BASEDIR}/archive"

printf '%s\n' $(cat << EOF
EXEC_USER=${EXEC_USER}
ARCHIVE_DIR=${ARCHIVE_DIR}
EOF
)


backup;

echo "### [finish] ${0##*/} ${@} ###"$'\n'$'\n'