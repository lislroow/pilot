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


## (stop) stop
function stop() {
  echo "+++ (stop) stop +++"
  for app_id in ${APP_ID_LIST[*]}
  do
    local ps_cmd="ps -ef | grep -v grep | grep -v tail |  grep -v .sh | grep ${app_id} | awk '{ print \$2 }'"
    echo "ps_cmd=${ps_cmd}"
    local _pid=$(eval "${ps_cmd}")
    
    if [ "${_pid}" != "" ]; then
      echo "stopping ${app_id}(pid:'${_pid}')"
      local kill_cmd="kill -15 ${_pid}"
      echo "kill_cmd=${kill_cmd}"
      eval "${kill_cmd}"
      
      i=1
      while [ $i -lt 600 ];
      do
        local _check_pid=$(eval "${ps_cmd}")
        echo "_check_pid=${_check_pid}"
        if [ "${_check_pid}" == "" ]; then
          echo "${app_id}(pid:'${_pid}') killed"
          break
        fi
        echo "wait for ${app_id}(pid:'${_pid}') killing"
        i=$(( $i + 1 ))
        sleep 1
      done
    else
      echo "${app_id} is not started"
    fi
  done
echo "--- //(stop) stop ---"
}


echo "+++ (runtime-env) +++"
EXEC_USER="tomcat"
APP_ID_LIST=()
case "$1" in
  dev)
    APP_ID_LIST+=(
      "dwww11"
      "dwww12"
      "dadm11"
      "dadm12"
    )
    ;;
  sta)
    APP_ID_LIST+=(
      "swww11"
      "swww12"
      "sadm11"
      "sadm12"
    )
    ;;
  www)
    APP_ID_LIST+=(
      "dwww11"
      "dwww12"
      "swww11"
      "swww12"
    )
    ;;
  adm)
    APP_ID_LIST+=(
      "dadm11"
      "dadm12"
      "sadm11"
      "sadm12"
    )
    ;;
  dw*1)
    APP_ID_LIST+=("dwww11")
    ;;
  dw*2)
    APP_ID_LIST+=("dwww12")
    ;;
  sw*1)
    APP_ID_LIST+=("swww11")
    ;;
  sw*2)
    APP_ID_LIST+=("swww12")
    ;;
  da*1)
    APP_ID_LIST+=("dadm11")
    ;;
  da*2)
    APP_ID_LIST+=("dadm12")
    ;;
  sa*1)
    APP_ID_LIST+=("sadm11")
    ;;
  sa*2)
    APP_ID_LIST+=("sadm12")
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
