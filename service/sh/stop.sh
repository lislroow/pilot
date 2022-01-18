#!/bin/bash

## env
BASEDIR="$( cd $( dirname "$0" ) && pwd -P)"
echo -e "\e[35m+++ [file] ${BASEDIR}/${0##*/} ${@} +++\e[m"
. ${BASEDIR}/include.sh

verboss="false"

function stop() {
  echo "+++ [func] ${BASEDIR}/${0##*/}:$FUNCNAME +++"
  
  local app_id_arr
  case "$1" in
    all)
      read -ra app_id_arr <<< $(GetSvrInfo "app_id" "ALL")
      ;;
    @(d|s)?(ev|ta))
      read -ra app_id_arr <<< $(GetSvrInfo "app_id" "profile_sys" "$1")
      ;;
    @(w|a)?(ww|dm))
      read -ra app_id_arr <<< $(GetSvrInfo "app_id" "app_name" "$1")
      ;;
    @(d|s)@(w|a)?(ww|dm))
      read -ra app_id_arr <<< $(GetSvrInfo "app_id" "app_id" "$1")
      ;;
    @(d|s)@(w|a)?(ww|dm)@(1|2)@(1|2))
      read -ra app_id_arr <<< $(GetSvrInfo "app_id" "app_id" "$1")
      ;;
  esac
  local tot="${#app_id_arr[@]}"
  local idx=1
  echo -e "## \e[36mtarget(${tot}):\e[m ${app_id_arr[@]}"
  
  for app_id in ${app_id_arr[@]}
  do
    local ps_cmd="ps -ef | grep -v grep | grep -v tail |  grep -v .sh | grep ${app_id} | awk '{ print \$2 }'"
    Log $verboss "ps_cmd=${ps_cmd}"
    local _pid=$(eval "${ps_cmd}")
    
    if [ "${_pid}" != "" ]; then
      echo -e "## \e[36m[${idx}/${tot}] ${app_id}: stop (${_pid})\e[m"
      local kill_cmd="kill -15 ${_pid}"
      echo -e "   kill(${app_id}): \e[30;42m${kill_cmd}\e[m"
      ExecCmd ${kill_cmd}
      
      local retry=1
      while [ $retry -lt 600 ];
      do
        local _check_pid=$(eval "${ps_cmd}")
        if [ "${_check_pid}" == "" ]; then
          Log $verboss "## stopped: ${app_id}(${_pid})"
          break
        fi
        Log $verboss "## stopping: ${app_id}(${_pid})"
        retry=$(( $retry + 1 ))
        sleep 2
      done
    else
      echo "## [${idx}/${tot}] ${app_id}: not running"
    fi
    idx=$(( $idx + 1 ))
  done
}



case "$1" in
  all)
    ;;
  @(d|s)?(ev|ta))
    ;;
  @(w|a)?(ww|dm))
    ;;
  @(d|s)@(w|a)?(ww|dm))
    ;;
  @(d|s)@(w|a)?(ww|dm)@(1|2)@(1|2))
    ;;
  *)
    echo "Usage: ${0##*/} [all|\${profile_sys}|\${app_name_c3}|\${app_id}]"
    stop "${PROFILE_SYS}";
    exit 0;
    ;;
esac


stop "$1";

