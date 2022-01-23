#!/bin/bash

## env
BASEDIR="$( cd $( dirname "$0" ) && pwd -P)"
echo -e "\e[35m+++ [file] ${BASEDIR}/${0##*/} ${@} +++\e[m"
. ${BASEDIR}/sh/include.sh

verboss="false"

function build() {
  echo "+++ [func] ${BASEDIR}/${0##*/}:$FUNCNAME: build maven project +++"
  
  local mvn_goals=()
  case "$1" in
    d*)
      mvn_goals+=("snapshot")
      ;;
    s*)
      mvn_goals+=("release")
      ;;
    *)
      mvn_goals+=("snapshot")
      mvn_goals+=("release")
      ;;
  esac
  case "$1" in
    all)
      read -ra app_name_arr <<< $(GetSvrInfo "app_name" "ALL")
      ;;
    @(d|s)@(w|a)*)
      read -ra app_name_arr <<< $(GetSvrInfo "app_name" "app_id" "$1")
      ;;
    @(w|a)*)
      read -ra app_name_arr <<< $(GetSvrInfo "app_name" "app_name" "$1")
      ;;
  esac
  
  tot=$((${#app_name_arr[@]}))
  idx=1
  for app_name in ${app_name_arr[@]}
  do
    local mvn_args=""
    mvn_args="${mvn_args} --file ${BASEDIR}/${app_name}/pom.xml"
    mvn_args="${mvn_args} -Dfile.encoding=utf-8"
    mvn_args="${mvn_args} -Dmaven.test.skip=true"
    mvn_args="${mvn_args} --update-snapshots"
    mvn_args="${mvn_args} --batch-mode"
    mvn_args="${mvn_args} --quiet"
    
    for mvn_goal in ${mvn_goals[@]}
    do
      local mvn_cmd="mvn ${mvn_args} "
      if [ "${mvn_goal}" == "snapshot" ]; then
        mvn_cmd="${mvn_cmd} clean deploy"
      elif [ "${mvn_goal}" == "release" ]; then
        mvn_cmd="${mvn_cmd} clean release:clean release:prepare release:perform"
        git_push_cmd="git push"
        echo "## ${git_push_cmd}"
        eval "${git_push_cmd}"
      fi
      echo -e "## \e[36m ${app_name}:\e[m \e[30;42m${mvn_cmd}\e[m"
      eval "${mvn_cmd}"
    done
  done
}



case "$1" in
  all)
    ;;
  @(d|s)@(w|a)*)
    ;;
  @(w|a)*)
    ;;
  *)
    echo "Usage: ${0##*/} [all|dw|sw|w|a]"
    exit 0;
    ;;
esac



build "$1";


