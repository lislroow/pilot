#!/bin/bash

echo "### [start] ${0##*/} ${@} ###"

## env
echo $"+++ (system-env) +++"
BASEDIR="$( cd $( dirname "$0" ) && pwd -P)"
ARCHIVE_DIR="${BASEDIR}/archive"

## include
. ${BASEDIR}/include.sh


## (backup) backup except for lastest jar
function backup() {
  echo "+++ (backup) backup except for lastest jar +++"
  
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
    if [ ! -e ${ARCHIVE_DIR} ]; then
      local mkdir_cmd="mkdir -p ${ARCHIVE_DIR}"
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
      mv_cmd="mv $(echo ${old_jar[@]} | tr -d '\n') ${ARCHIVE_DIR}"
      echo "mv_cmd=${mv_cmd}"
      ExecCmd ${mv_cmd}
    fi
  done
  
  echo "--- //(backup) backup except for lastest jar ---"
}



case "$1" in
  all)
    ;;
  @(w|a)?(ww|dm))
    ;;
  *)
    echo "Usage: ${0##*/} [all|w|a]"
    exit 0;
    ;;
esac

backup "$1";

echo "### [finish] ${0##*/} ${@} ###"$'\n'$'\n'