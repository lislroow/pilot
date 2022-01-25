#!/bin/bash

## env
BASEDIR="$( cd $( dirname "$0" ) && pwd -P)"
echo -e "\e[35m+++ [file] ${BASEDIR}/${0##*/} ${@} +++\e[m"
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
    if [ "${ps_result}" == "" ]; then
      echo -e "\e[31m[${app_id}]\e[m  process [${app_id}] not running"
    else
      echo -e "\e[36m[${app_id}]\e[m  ${ps_result}"
    fi
    local _pid=$(eval "echo ${ps_result} | awk '{ print \$2}'")
    local port
    read -r  port <<< $(GetSvrInfo "port" "app_id" "${app_id}")
    if [ "${_pid}" != "" ]; then
      local netstat_cmd="netstat -ntplu | grep ${_pid}"
      local netstat_result=$(eval "${netstat_cmd}")
      local listen_port=$(netstat -tnplu | grep ${_pid} | awk '{ if (match($4, /([0-9]*)$/, m)) print m[0] }')
      
      if [ "${listen_port}" != "" ]; then
        local http_code=$(curl --write-out "%{http_code}" --silent --output /dev/null "http://localhost:${listen_port}/")
        if [ "${http_code}" == "200" ]; then
          echo -e "\e[36m[${app_id}]\e[m  port \e[32m[${port}]\e[m OK: curl (http_code=${http_code})"
        else
          echo -e "\e[31m[${app_id}]\e[m  port \e[32m[${port}]\e[m fail: curl (http_code=${http_code})"
        fi
      else
        echo -e "\e[31m[${app_id}]\e[m  port \e[32m[${port}]\e[m fail: undetect listen port"
      fi
    else
      echo -e "\e[31m[${app_id}]\e[m  port \e[32m[${port}]\e[m fail: not running"
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


