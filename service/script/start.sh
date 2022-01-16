#!/bin/bash

echo "### [start] ${0##*/} ${@} ###"

## env
echo "+++ (system-env) +++"
BASEDIR="$( cd $( dirname "$0" ) && pwd -P)"
LOG_BASEDIR="/outlog/pilot"

## include
. ${BASEDIR}/include.sh


## (start) start
function start() {
  echo "+++ (start) start +++"
  
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
      stop_cmd="${BASEDIR}/stop.sh ${app_id}"
      echo "stop_cmd=${stop_cmd}"
      if [ $(whoami) == "root" ]; then
        su ${EXEC_USER} -c "${stop_cmd}"
      elif [ $(whoami) == ${EXEC_USER} ]; then
        eval "${stop_cmd}"
      else
        echo "current user "$(whoami)
        exit -1
      fi
    fi
    
    # console-logfile
    local log_filepath="${LOG_BASEDIR}/${app_id}-console.log"
    echo "log_filepath=${log_filepath}"
    if [ -e ${log_filepath} ]; then
      local curr_ts=`date +'%Y%m%d_%H%M%S'`
      local bak_filepath="${LOG_BASEDIR}/backup/${app_id}-console-${curr_ts}.log"
      echo "bak_filepath=${bak_filepath}"
      if [ ! -e "${LOG_BASEDIR}/backup" ]; then
        local mkdir_cmd="mkdir -p ${LOG_BASEDIR}/backup"
        echo "mkdir_cmd=${mkdir_cmd}"
        if [ $(whoami) == "root" ]; then
          su ${EXEC_USER} -c "${mkdir_cmd}"
        elif [ $(whoami) == ${EXEC_USER} ]; then
          eval "${mkdir_cmd}"
        else
          echo "current user "$(whoami)
          exit -1
        fi
      fi
      local cp_cmd="cp ${log_filepath} ${bak_filepath}"
      echo "cp_cmd=${cp_cmd}"
      if [ $(whoami) == "root" ]; then
        su ${EXEC_USER} -c "${cp_cmd}"
      elif [ $(whoami) == ${EXEC_USER} ]; then
        eval "${cp_cmd}"
      else
        echo "current user "$(whoami)
        exit -1
      fi
      local cat_cmd="cat /dev/null > ${log_filepath}"
      echo "cat_cmd=${cat_cmd}"
      if [ $(whoami) == "root" ]; then
        su ${EXEC_USER} -c "${cat_cmd}"
      elif [ $(whoami) == ${EXEC_USER} ]; then
        eval "${cat_cmd}"
      else
        echo "current user "$(whoami)
        exit -1
      fi
    fi
    
    local profile_sys app_name port
    read -r  profile_sys <<< $(GetSvrInfo "profile_sys" "app_id" "${app_id}")
    read -r  app_name <<< $(GetSvrInfo "app_name" "app_id" "${app_id}")
    read -r  port <<< $(GetSvrInfo "port" "app_id" "${app_id}")
    
    local jar_file
    if [ "$1" != "all" ] && [ "$2" != "" ]; then
      jar_file=${BASEDIR}/$2
      if [ ! -e ${jar_file} ]; then
        echo "jar_file(${jar_file}) does not exist"
        exit -1
      fi
    else
      find_cmd="ls -rt ${BASEDIR}/${app_name}*.jar | sort -V | tail -n 1"
      echo "find_cmd=${find_cmd}"
      jar_file=$(eval "${find_cmd}")
    fi
    
    local java_opts=""
    java_opts="${java_opts} -Dspring.profiles.active=${profile_sys}"
    java_opts="${java_opts} -Dspring.output.ansi.enabled=always"
    java_opts="${java_opts} -Dapp.name=${app_name}"
    java_opts="${java_opts} -Dapp.id=${app_id}"
    java_opts="${java_opts} -Dserver.port=${port}"
    
    local java_cmd="nohup $JAVA_HOME/bin/java ${java_opts} -jar ${jar_file} > ${log_filepath} 2>&1 &"
    echo "java_cmd=${java_cmd}"
    if [ $(whoami) == "root" ]; then
      su ${EXEC_USER} -c "${java_cmd}"
    elif [ $(whoami) == ${EXEC_USER} ]; then
      eval "${java_cmd}"
    else
      echo "current user "$(whoami)
      exit -1
    fi
    
    echo "ps_cmd=${ps_cmd}"
    local _pid=$(eval "${ps_cmd}")
    echo "_pid=${_pid}"
    
    if [ "${_pid}" == "" ]; then
      echo "${app_id} is not started"
    else
      echo "${app_id}(pid:'${_pid}') starting ..."
    fi
    
    i=1
    while [ $i -lt 600 ];
    do
      local http_code=$(curl --write-out "%{http_code}" --silent --output /dev/null "http://localhost:${port}/")
      if [ "${http_code}" == "200" ]; then
        echo "${app_id}(pid:'${_pid}') started"
        break
      fi
      echo "${app_id}(pid:'${_pid}') booting ..."
      i=$(( $i + 1 ))
      sleep 3
    done
  done
  
  echo "--- //(start) start ---"
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



start "$1" "$2";

echo "### [finish] ${0##*/} ${@} ###"$'\n'$'\n'
