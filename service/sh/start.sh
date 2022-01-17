#!/bin/bash

## env
BASEDIR="$( cd $( dirname "$0" ) && pwd -P)"
echo -e "\e[35m### [file] ${BASEDIR}/${0##*/} ${@} ###\e[m"
. ${BASEDIR}/include.sh

verboss="false"

function start() {
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
    @(d|s)@(w|a)?(ww|dm)@(1|2)@(1|2))
      read -ra app_id_arr <<< $(GetSvrInfo "app_id" "app_id" "$1")
      ;;
  esac
  echo -e "## \e[36mtarget:\e[m ${app_id_arr[@]}"
  
  
  for app_id in ${app_id_arr[@]}
  do
    local ps_cmd="ps -ef | grep -v grep | grep -v tail |  grep -v .sh | grep ${app_id} | awk '{ print \$2 }'"
    Log $verboss "ps_cmd=${ps_cmd}"
    local _pid=$(eval "${ps_cmd}")
    
    if [ "${_pid}" != "" ]; then
      stop_cmd="${BASEDIR}/stop.sh ${app_id}"
      Log $verboss "stop_cmd=${stop_cmd}"
      ExecCmd ${stop_cmd}
    fi
    
    # console-logfile
    local log_filepath="${LOG_BASEDIR}/${app_id}-console.log"
    Log $verboss "log_filepath=${log_filepath}"
    if [ -e ${log_filepath} ]; then
      local curr_ts=`date +'%Y%m%d_%H%M%S'`
      local bak_filepath="${LOG_BASEDIR}/backup/${app_id}-console-${curr_ts}.log"
      Log $verboss "bak_filepath=${bak_filepath}"
      if [ ! -e "${LOG_BASEDIR}/backup" ]; then
        local mkdir_cmd="mkdir -p ${LOG_BASEDIR}/backup"
        Log $verboss "mkdir_cmd=${mkdir_cmd}"
        ExecCmd ${mkdir_cmd}
      fi
      
      local cp_cmd="cp ${log_filepath} ${bak_filepath}"
      Log $verboss "cp_cmd=${cp_cmd}"
      ExecCmd ${cp_cmd}
      echo -e "## move: ${bak_filepath}"
      
      local cat_cmd="cat /dev/null > ${log_filepath}"
      Log $verboss "cat_cmd=${cat_cmd}"
      ExecCmd ${cat_cmd}
    fi
    
    local profile_sys app_name port
    read -r  profile_sys <<< $(GetSvrInfo "profile_sys" "app_id" "${app_id}")
    read -r  app_name <<< $(GetSvrInfo "app_name" "app_id" "${app_id}")
    read -r  port <<< $(GetSvrInfo "port" "app_id" "${app_id}")
    
    local jar_file
    if [ "$1" != "all" ] && [ "$2" != "" ]; then
      jar_file=${BASEDIR}/$2
      if [ ! -e ${jar_file} ]; then
        echo -e "\e[31m${jar_file} does not exist\e[m"
        exit -1
      fi
    else
      find_cmd="ls -rt ${BASEDIR}/${app_name}*.jar | sort -V | tail -n 1"
      Log $verboss "find_cmd=${find_cmd}"
      jar_file=$(eval "${find_cmd}")
      echo -e "## \e[36mtarget jar:\e[m ${jar_file}"
    fi
    
    local java_opts=""
    java_opts="${java_opts} -Dspring.profiles.active=${profile_sys}"
    java_opts="${java_opts} -Dapp.name=${app_name}"
    java_opts="${java_opts} -Dapp.id=${app_id}"
    java_opts="${java_opts} -Dserver.port=${port}"
    
    echo -e "## \e[36mstart:\e[m ${app_id}"
    local java_cmd="nohup $JAVA_HOME/bin/java ${java_opts} -jar ${jar_file} > ${log_filepath} 2>&1 &"
    echo -e "## \e[36mjava:\e[m ${java_cmd}"
    Log $verboss "java_cmd=${java_cmd}"
    ExecCmd ${java_cmd}
    
    Log $verboss "ps_cmd=${ps_cmd}"
    local _pid=$(eval "${ps_cmd}")
    Log $verboss "_pid=${_pid}"
    
    if [ "${_pid}" == "" ]; then
      echo "## ${app_id}: not started"
    else
      echo -e "## \e[36mstarting: \e[m${app_id}(${_pid})"
    fi
    
    local i=1
    while [ $i -lt 600 ];
    do
      local http_code=$(curl --write-out "%{http_code}" --silent --output /dev/null "http://localhost:${port}/")
      if [ "${http_code}" == "200" ]; then
        echo -e "## \e[36mstarted: \e[m ${app_id}(${_pid})"
        break
      fi
      echo "## booting: ${app_id}(${_pid}) "
      i=$(( $i + 1 ))
      sleep 4
    done
  done
}



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
    start "${PROFILE_SYS}";
    exit 0;
    ;;
esac



start "$1" "$2";

