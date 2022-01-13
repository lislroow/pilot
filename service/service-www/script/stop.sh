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
echo "--- (stop) stop ---"
}


echo "+++ (runtime-env) +++"
EXEC_USER="tomcat"
APP_ID=$1
case ${APP_ID} in
  dwww11)
    ;;
  dwww12)
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
echo "--- (runtime-env) ---"

stop;

echo "### [finish] ${0##*/} ${@} ###"$'\n'
