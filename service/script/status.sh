#!/bin/bash

echo "### [start] ${0##*/} ${@} ###"

echo "+++ (system-env) +++"
SCRIPT_DIR="$( cd $( dirname "$0" ) && pwd -P)"

printf '%s\n' $(cat << EOF
SCRIPT_DIR=${SCRIPT_DIR}
EOF
)


## (status) status
function status() {
  echo "+++ (status) status +++"
  for APP_ID in ${APP_ID_LIST[*]}
  do
    echo "--- ${APP_ID} ---"
    PS_CMD="ps aux | grep -v grep | grep -v tail |  grep -v .sh | grep ${APP_ID}"
    eval "${PS_CMD}"
    _PID=$(eval "${PS_CMD} | awk '{ print \$2}'")
    if [ "${_PID}" != "" ]; then
      netstat -ntplu | grep ${_PID}
      SERVER_PORT=$(netstat -tnplu | grep ${_PID} | awk '{ if (match($4, /([0-9]*)$/, m)) print m[0] }')
      if [ "${SERVER_PORT}" != "" ]; then
        HTTP_CODE=$(curl --write-out "%{http_code}" --silent --output /dev/null "http://localhost:${SERVER_PORT}/")
        if [ "${HTTP_CODE}" == "200" ]; then
          echo "${APP_ID} is avaiable. (SERVER_PORT=${SERVER_PORT})"
        else
          echo "${APP_ID} is not avaiable. (SERVER_PORT=${SERVER_PORT})"
        fi
      else
        echo "${APP_ID} is not avaiable."
      fi
    else
      echo "${APP_ID} is not running"
    fi
    echo ""
  done
  echo "--- //(status) status ---"
}



echo "+++ (runtime-env) +++"
APP_ID_LIST=(
  "dwww11"
  "dwww12"
  "swww11"
  "swww12"
  "dadm11"
  "dadm12"
  "sadm11"
  "sadm12"
)
printf '%s\n' $(cat << EOF
APP_ID_LIST=${APP_ID_LIST[*]}
EOF
)



status;


echo "### [finish] ${0##*/} ${@} ###"$'\n'$'\n'
