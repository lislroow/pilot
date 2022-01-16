#!/bin/bash

echo "### [start] ${0##*/} ${@} ###"

echo "+++ (system-env) +++"
BASEDIR="$( cd $( dirname "$0" ) && pwd -P)"

printf '%s\n' $(cat << EOF
BASEDIR=${BASEDIR}
EOF
)


## (status) status
function status() {
  echo "+++ (status) status +++"
  for app_id in ${APP_ID_LIST[@]}
  do
    echo "--- ${app_id} ---"
    local ps_cmd="ps aux | grep -v grep | grep -v tail |  grep -v .sh | grep ${app_id}"
    eval "${ps_cmd}"
    local _pid=$(eval "${ps_cmd} | awk '{ print \$2}'")
    if [ "${_pid}" != "" ]; then
      netstat -ntplu | grep ${_pid}
      local server_port=$(netstat -tnplu | grep ${_pid} | awk '{ if (match($4, /([0-9]*)$/, m)) print m[0] }')
      if [ "${server_port}" != "" ]; then
        local http_code=$(curl --write-out "%{http_code}" --silent --output /dev/null "http://localhost:${server_port}/")
        if [ "${http_code}" == "200" ]; then
          echo "${app_id} is avaiable. (server_port=${server_port})"
        else
          echo "${app_id} is not avaiable. (server_port=${server_port})"
        fi
      else
        echo "${app_id} is not avaiable."
      fi
    else
      echo "${app_id} is not running"
    fi
    echo ""
  done
  echo "--- //(status) status ---"
}



echo "+++ (runtime-env) +++"
PROFILE_SYS=$1
case "${PROFILE_SYS}" in
  d*)
    APP_ID_LIST=(
      "dwww11"
      "dwww12"
      "dadm11"
      "dadm12"
    )
    ;;
  s*)
    APP_ID_LIST=(
      "swww11"
      "swww12"
      "sadm11"
      "sadm12"
    )
    ;;
  *)
    echo "Usage: ${0##*/} [dev|sta]"
    exit -1
    ;;
esac


printf '%s\n' $(cat << EOF
APP_ID_LIST=${APP_ID_LIST[@]}
EOF
)



status;


echo "### [finish] ${0##*/} ${@} ###"$'\n'$'\n'
