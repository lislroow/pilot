#!/bin/bash

## env
BASEDIR="$( cd $( dirname "$0" ) && pwd -P)"
echo "### [file] ${BASEDIR}/${0##*/} ${@} ###"
. ${BASEDIR}/include.sh


function status() {
  echo "+++ [func] ${BASEDIR}/${0##*/}:$FUNCNAME +++"
  
  local app_id_arr
  case "$1" in
    all)
      read -ra app_id_arr <<< $(GetSvrInfo "app_id" "ALL")
      ;;
    @(d|s)?(ev|ta))
      read -ra app_id_arr <<< $(GetSvrInfo "app_id" "profile_sys" "$1")
      ;;
  esac
  
  for app_id in ${app_id_arr[@]}
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
}




case "$1" in
  all)
    ;;
  @(d|s)?(ev|ta))
    ;;
  *)
    echo "Usage: ${0##*/} [all|\${profile_sys}]"
    status "${PROFILE_SYS}";
    exit 0;
    ;;
esac

status "$1";


