#!/bin/bash

## env
BASEDIR="$( cd $( dirname "$0" ) && pwd -P)"
echo -e "\e[35m+++ [file] ${BASEDIR}/${0##*/} ${@} +++\e[m"
. ${BASEDIR}/include.sh


function backup() {
  echo "+++ [func] ${BASEDIR}/${0##*/}:${FUNCNAME}: backup except for lastest jar"
  
  local verboss="false"
  case "$1" in
    @(v)?(erboss))
      verboss="true"
      ;;
  esac
  
  read -ra app_name_arr <<< $(GetSvrInfo "app_name" "ALL")
  
  for app_name in ${app_name_arr[@]}
  do
    read -r  app_home <<< $(GetSvrInfo "app_home" "profile_sys" "${PROFILE_SYS}" "app_name" "${app_name}")
    local backup_dir="${app_home}/old"
    if [ ! -e "${backup_dir}" ]; then
      local mkdir_cmd="mkdir -p ${backup_dir}"
      Log $verboss "mkdir_cmd=${mkdir_cmd}"
      ExecCmd ${mkdir_cmd}
    fi
    
    local latest_cmd="ls -rt ${BASEDIR}/${app_name}*.jar | tail -n 1"
    Log $verboss "latest_cmd=${latest_cmd}"
    latest_jar=$(eval "${latest_cmd}")
    
    old_cmd="find ${BASEDIR} -maxdepth 1 -type f ! -newer ${latest_jar} -name '${app_name}*.jar' ! -samefile ${latest_jar}"
    Log $verboss "old_cmd=${old_cmd}"
    old_jar=$(eval "${old_cmd}")
    Log $verboss "latest_jar=${latest_jar}"
    Log $verboss "old_jar=${old_jar[@]}"
    
    if [ "${old_jar[@]}" != "" ]; then
      mv_cmd="mv ${old_jar[*]} ${backup_dir}"
      Log $verboss "mv_cmd=${mv_cmd}"
      ExecCmd ${mv_cmd}
      
      local tot=${#old_jar[@]}
      local idx=1
      for file in ${old_jar[@]}
      do
        if [ -e "${backup_dir}/${file##*/}" ]; then
          echo -e "[${app_name}] [${idx}/${tot}] \e[36m${file##*/}\e[m moved to \e[36m${backup_dir##*/}/${file##*/}\e[m"
        fi
        idx=$(( $idx + 1 ))
      done
    else
      echo -e "\e[31mnothing has moved\e[m"
    fi
  done
}



case "$1" in
  @(v)?(erboss))
    ;;
  -h)
    echo "Usage: ${0##*/} [verboss]"
    exit 0;
    ;;
  *)
    ;;
esac

backup "$1";

