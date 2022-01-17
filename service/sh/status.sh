#!/bin/bash

## env
BASEDIR="$( cd $( dirname "$0" ) && pwd -P)"
echo -e "\e[35m### [file] ${BASEDIR}/${0##*/} ${@} ###\e[m"
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
    local ps_cmd="ps aux | grep -v grep | grep -v tail |  grep -v .sh | grep ${app_id}"
    local ps_result=$(eval "${ps_cmd}")
    echo -e "\e[36m[${app_id}]\e[m  ${ps_result}"
    local _pid=$(eval "${ps_cmd} | awk '{ print \$2}'")
    if [ "${_pid}" != "" ]; then
      local netstat_cmd="netstat -ntplu | grep ${_pid}"
      local netstat_result=$(eval "${netstat_cmd}")
      echo -e "\e[36m[${app_id}]\e[m  ${netstat_result}"
      local server_port=$(netstat -tnplu | grep ${_pid} | awk '{ if (match($4, /([0-9]*)$/, m)) print m[0] }')
      if [ "${server_port}" != "" ]; then
        local http_code=$(curl --write-out "%{http_code}" --silent --output /dev/null "http://localhost:${server_port}/")
        if [ "${http_code}" == "200" ]; then
          echo -e "\e[36m[${app_id}]\e[m  OK: curl test (http_code=${http_code})"
        else
          echo -e "\e[31m[${app_id}]\e[m  fail: curl test (http_code=${http_code})"
        fi
      else
        echo -e "\e[31m[${app_id}]\e[m  listen port is not found"
      fi
    else
      echo -e "\e[31m[${app_id}]\e[m  isn't running"
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


