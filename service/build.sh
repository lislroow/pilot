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
    #mvn_args="${mvn_args} --quiet"
    
    local nx_repo_id="maven-release"
    local nx_group_id="mgkim/framework"
    local nx_artifact_id="framework-bom"
    read -ra framework_ver <<< $(GetFrameworkVer "${nx_repo_id}" "${nx_group_id}" "${nx_artifact_id}")
    
    nx_repo_id="maven-release"
    nx_group_id="mgkim/service"
    nx_artifact_id="service-lib"
    read -ra service_ver <<< $(GetFrameworkVer "${nx_repo_id}" "${nx_group_id}" "${nx_artifact_id}")
    
    for mvn_goal in ${mvn_goals[@]}
    do
      if [ "${mvn_goal}" == "snapshot" ]; then
        local mvn_cmd="mvn ${mvn_args} "
        mvn_cmd="${mvn_cmd} clean deploy"
        echo -e "## \e[36m ${app_name}:\e[m \e[30;42m${mvn_cmd}\e[m"
        eval "${mvn_cmd}"
      elif [ "${mvn_goal}" == "release" ]; then
        local mvn_cmd="mvn ${mvn_args} -Prelease -Darguments=\"-Dframework.version=${framework_ver} -Dservice-lib.version=${service_ver}\""
        mvn_cmd="${mvn_cmd} clean release:clean release:prepare release:perform"
        echo -e "## \e[36m ${app_name}:\e[m \e[30;42m${mvn_cmd}\e[m"
        eval "${mvn_cmd}"
        git_push_cmd="git push origin"
        echo "## ${git_push_cmd}"
        eval "${git_push_cmd}"
      fi
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


