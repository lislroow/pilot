#!/bin/bash

echo "### [start] ${0##*/} ${@} ###"

## env
echo "+++ (system-env) +++"
BASEDIR="$( cd $( dirname "$0" ) && pwd -P)"

## include
. ${BASEDIR}/include.sh


## (stop) stop
function stop() {
  echo "+++ (stop) stop +++"
  
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
    @(d|s)@(w|a)?(ww|dm)@(1|2)@(1|2))
      read -ra app_id_arr <<< $(GetSvrInfo "app_id" "app_id" "$1")
      ;;
  esac
  echo "app_id_arr=${app_id_arr[@]}"
  
  
  for app_id in ${app_id_arr[@]}
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
case "$1" in
  all)
    ;;
  @(d|s)?(ev|ta))
    ;;
  @(w|a)?(ww|dm))
    ;;
  @(d|s)@(w|a)?(ww|dm)@(1|2)@(1|2))
    ;;
  *)
    echo "Usage: ${0##*/} [all|\${profile_sys}|\${app_name_c3}|\${app_id}]"
    exit -1
    ;;
esac


stop "$1" "$2";

echo "### [finish] ${0##*/} ${@} ###"$'\n'$'\n'
