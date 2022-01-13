#!/bin/bash

echo "### [start] ${0##*/} ${@} ###"

## env
echo "+++ (system-env) +++"
SCRIPT_DIR="$( cd $( dirname "$0" ) && pwd -P)"
BASEDIR="$( cd ${SCRIPT_DIR} && pwd -P)"

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



## (deploy) deploying trigger
function deploy() {
  echo "+++ (deploy) deploying trigger +++"
  for SVR in ${SVR_LIST[*]}
  do
    ssh ${EXEC_USER}@${SVR} "${APP_HOME}/deploy.sh ${PROFILE_SYS}"
  done
  
  echo "--- (deploy) deploying trigger ---"$
}


echo "+++ (runtime-env) +++"
EXEC_USER="tomcat"
PROFILE_SYS=$1

if [ "$1" == "" ]; then
  PROFILE_SYS="dev"
fi

case ${PROFILE_SYS} in
  dev)
    SVR_LIST=('172.28.200.30')
    APP_HOME="/app/pilot-dev"
    ;;
  sta*)
    SVR_LIST=('172.28.200.30')
    APP_HOME="/app/pilot-sta"
    ;;
  *)
    exit -1
    ;;
esac


printf '%s\n' $(cat << EOF
EXEC_USER=${EXEC_USER}
PROFILE_SYS=${PROFILE_SYS}
SVR_LIST=${SVR_LIST}
APP_HOME=${APP_HOME}
EOF
)
echo "--- (runtime-env) ---"


deploy;


echo "### [finish] ${0##*/} ${@} ###"$'\n'
