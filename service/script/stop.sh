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


## (stop) stop
function stop() {
  echo "+++ (stop) stop +++"
  PS_CMD="ps -ef | grep -v grep | grep -v tail |  grep -v .sh | grep ${APP_ID} | awk '{ print \$2 }'"
  echo "${PS_CMD}"
  _PID=$(eval "${PS_CMD}")
  
  if [ "${_PID}" != "" ]; then
    echo "stopping ${APP_ID}(pid:'${_PID}')"
    kill -15 ${_PID}
    
    i=1
    while [ $i -lt 600 ];
    do
      _CHECK_PID=$(eval "${PS_CMD}")
      if [ "${_CHECK_PID}" == "" ]; then
        echo "${APP_ID}(pid:'${_PID}') killed"
        break
      fi
      echo "wait for ${APP_ID}(pid:'${_PID}') killing"
      i=$(( $i + 1 ))
      sleep 1
    done
  else
    echo "${APP_ID} is not started"
  fi
echo "--- //(stop) stop ---"
}


echo "+++ (runtime-env) +++"
EXEC_USER="tomcat"
APP_ID=$1
case "${APP_ID}" in
  dw*1)
    APP_ID="dwww11"
    ;;
  dw*2)
    APP_ID="dwww12"
    ;;
  sw*1)
    APP_ID="swww11"
    ;;
  sw*2)
    APP_ID="swww12"
    ;;
  da*1)
    APP_ID="dadm11"
    ;;
  da*2)
    APP_ID="dadm12"
    ;;
  sa*1)
    APP_ID="sadm11"
    ;;
  sa*2)
    APP_ID="sadm12"
    ;;
  *)
    exit -1
    ;;
esac

printf '%s\n' $(cat << EOF
EXEC_USER=${EXEC_USER}
APP_ID=${APP_ID}
EOF
)


stop;

echo "### [finish] ${0##*/} ${@} ###"$'\n'$'\n'
