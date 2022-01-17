#!/bin/bash

echo "### [file] ${0##*/} ${@} ###"

## env
BASEDIR="$( cd $( dirname "$0" ) && pwd -P)"
. ${BASEDIR}/include.sh


function backup() {
  echo "+++ [func] ${BASEDIR}/${0##*/}:${FUNCNAME}: backup except for lastest jar"
  
  case "$1" in
    all)
      read -ra app_name_arr <<< $(GetSvrInfo "app_name" "ALL")
      ;;
    @(w|a)?(ww|dm))
      read -ra app_name_arr <<< $(GetSvrInfo "app_name" "app_name" "$1")
      ;;
  esac
  
  for app_name in ${app_name_arr[@]}
  do
    read -r  app_home <<< $(GetSvrInfo "app_home" "profile_sys" "${PROFILE_SYS}" "app_name" "${app_name}")
    local backup_dir="${app_home}/bak"
    if [ ! -e "${backup_dir}" ]; then
      local mkdir_cmd="mkdir -p ${backup_dir}"
      echo "mkdir_cmd=${mkdir_cmd}"
      ExecCmd ${mkdir_cmd}
    fi
    
    local latest_cmd="ls -rt ${BASEDIR}/${app_name}*.jar | tail -n 1"
    echo "latest_cmd=${latest_cmd}"
    latest_jar=$(eval "${latest_cmd}")
    
    old_cmd="find ${BASEDIR} -maxdepth 1 -type f ! -newer ${latest_jar} -name '${app_name}*.jar' ! -samefile ${latest_jar}"
    echo "old_cmd=${old_cmd}"
    old_jar=$(eval "${old_cmd}")
    
    echo "latest_jar=${latest_jar}"
    echo "old_jar=${old_jar[@]}"
    
    if [ "${old_jar[@]}" != "" ]; then
      mv_cmd="mv ${old_jar[*]} ${backup_dir}"
      echo "mv_cmd=${mv_cmd}"
      ExecCmd ${mv_cmd}
    fi
  done
}



case "$1" in
  all)
    ;;
  @(w|a)?(ww|dm))
    ;;
  *)
    echo "Usage: ${0##*/} [all|w|a]"
    backup "all";
    exit 0;
    ;;
esac

backup "$1";

