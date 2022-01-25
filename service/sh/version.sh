#!/bin/bash

## env
BASEDIR="$( cd $( dirname "$0" ) && pwd -P)"
echo -e "\e[35m+++ [file] ${BASEDIR}/${0##*/} ${@} +++\e[m"
. ${BASEDIR}/include.sh

verboss="false"

function artifact() {
  echo "+++ [func] $FUNCNAME +++"
  
  local app_name_arr
  read -ra app_name_arr <<< $(GetSvrInfo "app_name" "ALL")
  local nx_repo_id_arr=("maven-snapshot" "maven-release")
  local nx_group_id="mgkim/service"
  
  for nx_repo_id in ${nx_repo_id_arr[@]}
  do
    for app_name in ${app_name_arr[@]}
    do
      local nx_artifact_id="${app_name}"
      
      read -ra nx_info <<< $(GetJarNxUrl "https://nexus/repository/${nx_repo_id}/${nx_group_id}/${nx_artifact_id}")
      local download_url="${nx_info[0]}"
      local jar_file="${nx_info[1]}"
      Log $verboss "nx_info=${nx_info[@]}"
      
      echo -e "## \e[32m${nx_repo_id}\e[m ${nx_artifact_id} \e[36m${jar_file}\e[m"
    done
  done
}




case "$1" in
  -h)
    echo "Usage: ${0##*/} [verboss]"
    exit 0;
    ;;
  *)
    ;;
esac

artifact
