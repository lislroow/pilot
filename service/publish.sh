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




## (deploy) deploying trigger
function deploy() {
  echo "+++ (deploy) deploying trigger +++"
  for svr in ${SVR_LIST[*]}
  do
    ssh ${EXEC_USER}@${svr} "${APP_HOME}/deploy.sh ${PROFILE_SYS} ${APP_NAME}"
  done
  
  echo "--- //(deploy) deploying trigger ---"
}


echo "+++ (runtime-env) +++"
EXEC_USER="tomcat"
PROFILE_SYS=$1
case "${PROFILE_SYS}" in
  dev)
    SVR_LIST=("172.28.200.30")
    APP_HOME="/app/pilot-dev"
    ;;
  sta)
    SVR_LIST=("172.28.200.30")
    APP_HOME="/app/pilot-sta"
    ;;
  -h)
    echo "Usage: ${0##*/} [dev|sta] [w|a]"
    exit 0;
    ;;
  *)
    echo "Usage: ${0##*/} [dev|sta] [w|a]"
    exit -1
    ;;
esac
APP_NAME=$2
case "${APP_NAME}" in
  *w*)
    APP_NAME="service-www"
    ;;
  *a*)
    APP_NAME="service-adm"
    ;;
  *)
    exit -1;
    ;;
esac


printf '%s\n' $(cat << EOF
EXEC_USER=${EXEC_USER}
PROFILE_SYS=${PROFILE_SYS}
SVR_LIST=${SVR_LIST}
APP_HOME=${APP_HOME}
APP_NAME=${APP_NAME}
EOF
)



deploy;


echo "### [finish] ${0##*/} ${@} ###"$'\n'$'\n'
