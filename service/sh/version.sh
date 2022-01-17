#!/bin/bash

## env
BASEDIR="$( cd $( dirname "$0" ) && pwd -P)"
echo "### [file] ${BASEDIR}/${0##*/} ${@} ###"
. ${BASEDIR}/include.sh


function artifact() {
  echo "+++ [func] $FUNCNAME +++"
  
  verboss="false"
  case "$1" in
    @(v)?(erboss))
      verboss="true"
      ;;
  esac
  
  local app_name_arr
  read -ra app_name_arr <<< $(GetSvrInfo "app_name" "ALL")
  local nx_repo_id_arr=("maven-snapshot" "maven-release")
  local nx_group_id="mgkim/service"
  
  for nx_repo_id in ${nx_repo_id_arr[@]}
  do
    for app_name in ${app_name_arr[@]}
    do
      local nx_artifact_id="${app_name}"
      
      read -ra nx_info <<< $(GetLatestArtifact "${nx_repo_id}" "${nx_group_id}" "${nx_artifact_id}")
      local download_url="${nx_info[0]}"
      local jar_file="${nx_info[1]}"
      if [ "${verboss}" == "true" ]; then echo "nx_info=${nx_info[@]}" fi
      
      echo "(${nx_repo_id}) ${nx_artifact_id}: ${jar_file}"
    done
  done
}




case "$1" in
  @(v)?(erboss))
    artifact "$1"
    ;;
  -h)
    echo "Usage: ${0##*/} [verboss]"
    exit 0;
    ;;
  *)
    artifact "$1"
    ;;
esac


