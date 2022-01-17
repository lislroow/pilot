#!/bin/bash

echo "### [file] ${0##*/} ${@} ###"

## env
BASEDIR="$( cd $( dirname "$0" ) && pwd -P)"
. ${BASEDIR}/include.sh


function deploy() {
  echo "+++ [func] ${BASEDIR}/${0##*/}:$FUNCNAME: backup except for lastest jar"
  
  local app_name_arr
  case "$1" in
    @(d|s)?(ev|ta))
      read -ra app_name_arr <<< $(GetSvrInfo "app_name" "profile_sys" "$1")
      ;;
    @(d|s)@(w|a)?(ww|dm))
      read -ra app_name_arr <<< $(GetSvrInfo "app_name" "app_id" "$1")
      ;;
  esac
  echo "app_name_arr=${app_name_arr[@]}"
  
  
  # nexus-metadata
  for app_name in ${app_name_arr[@]}
  do
    local profile_sys
    read -r  profile_sys <<< $(GetSvrInfo "profile_sys" "app_id" "$1")
    local nx_repo_id
    local nx_group_id="mgkim/service"
    local nx_artifact_id="${app_name}"
    
    case "$1" in
      @(d)*) nx_repo_id="maven-snapshot";;
      @(s)*) nx_repo_id="maven-release" ;;
    esac
    
    read -ra nx_info <<< $(GetLatestArtifact "${nx_repo_id}" "${nx_group_id}" "${nx_artifact_id}")
    echo "nx_info=${nx_info[@]}"
    local download_url="${nx_info[0]}"
    local jar_file="${nx_info[1]}"
    
    # download
    download_cmd="curl --silent --output ${BASEDIR}/${jar_file} ${download_url}"
    echo "download_cmd=${download_cmd}"
    ExecCmd ${download_cmd}
    
    # final_name
    #local md5str=$(md5sum ${BASEDIR}/${jar_file} | awk '{ print substr($1, 1, 4) }')
    #local final_name=${jar_file%.*}_${md5str}.${jar_file##*.}
    #echo "final_name=${final_name}"
    local final_name=${jar_file}
    echo "final_name=${final_name}"
    local mv_cmd="mv ${BASEDIR}/${jar_file} ${BASEDIR}/${final_name}"
    echo "mv_cmd=${mv_cmd}"
    ExecCmd ${mv_cmd}
    
    
    # stop / start
    local app_id_arr
    read -ra app_id_arr <<< $(GetSvrInfo "app_id" "profile_sys" "${1:0:1}" "app_name" "${app_name}")
    
    for app_id in ${app_id_arr[@]}
    do
      read -r  app_home <<< $(GetSvrInfo "app_home" "app_id" "${app_id}")
      
      # stop
      local stop_cmd="${app_home}/stop.sh ${app_id}"
      echo "stop_cmd=${stop_cmd[*]}"
      ExecCmd ${stop_cmd}
      
      # start
      local start_cmd="${app_home}/start.sh ${app_id} ${final_name}"
      echo "start_cmd=${start_cmd[@]}"
      ExecCmd ${start_cmd}
    done
  done
}



case "$1" in
  @(d|s)?(ev|ta))
    ;;
  @(d|s)@(w|a)?(ww|dm))
    ;;
  *)
    echo "Usage: ${0##*/} [all|d|s|w|a|dw|da|sw|sa]"
    deploy "${PROFILE_SYS}";
    exit 0;
    ;;
esac

deploy "$1";

