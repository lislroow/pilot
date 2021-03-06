#!/bin/bash

## env
BASEDIR="$( cd $( dirname "$0" ) && pwd -P)"
echo -e "\e[35m+++ [file] ${BASEDIR}/${0##*/} ${@} +++\e[m"
. ${BASEDIR}/include.sh

verboss="false"

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
  Log $verboss "app_name_arr=${app_name_arr[@]}"
  
  
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
    
    read -ra nx_info <<< $(GetNxUrl "https://nexus/repository/${nx_repo_id}/${nx_group_id}/${nx_artifact_id}")
    Log $verboss "nx_info=${nx_info[@]}"
    local download_url="${nx_info[0]}"
    local jar_file="${nx_info[1]}"
    echo -e "## \e[36mchecked from nexus:\e[m ${download_url}"
    
    # download
    download_cmd="curl --silent --output ${BASEDIR}/${jar_file} ${download_url}"
    Log $verboss "download_cmd=${download_cmd}"
    ExecCmd ${download_cmd}
    echo -e "## \e[36mdownloaded from nexus:\e[m ${BASEDIR}/${jar_file}"
    
    # final_name
    local final_name=${jar_file}
    Log $verboss "final_name=${final_name}"
    
    
    # stop / start
    local app_id_arr
    read -ra app_id_arr <<< $(GetSvrInfo "app_id" "profile_sys" "${1:0:1}" "app_name" "${app_name}")
    
    echo -e "## \e[36mdeploy target:\e[m app_id=[${app_id_arr[@]}]"
    for app_id in ${app_id_arr[@]}
    do
      read -r  app_home <<< $(GetSvrInfo "app_home" "app_id" "${app_id}")
      
      # stop
      echo -e "## \e[36mstop to:\e[m ${app_id}"
      local stop_cmd="${app_home}/stop.sh ${app_id}"
      Log $verboss "stop_cmd=${stop_cmd[*]}"
      ExecCmd ${stop_cmd}
      
      # start
      echo -e "## \e[36mstart to:\e[m ${app_id}"
      local start_cmd="${app_home}/start.sh ${app_id} ${final_name}"
      Log $verboss "start_cmd=${start_cmd[@]}"
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

