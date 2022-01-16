#!/bin/bash

echo "### [start] ${0##*/} ${@} ###"

## env
echo "+++ (system-env) +++"
BASEDIR="$( cd $( dirname "$0" ) && pwd -P)"

## include
. ${BASEDIR}/include.sh


## (deploy) deploy
function deploy() {
  echo "+++ (deploy) deploy +++"
  
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
    case "$1" in
      @(d)*)
        # lastest artifact (maven-snapshot)
        local nexus_url="https://nexus/repository/maven-snapshot"
        local metadata_url="${nexus_url}/mgkim/service/${app_name}/maven-metadata.xml"
        echo "metadata_url=${metadata_url}"
        local app_ver=$(curl -s ${metadata_url} | xmllint --xpath "//version[last()]/text()" -)
        echo "app_ver=${app_ver}"
        
        local metadata_url="${nexus_url}/mgkim/service/${app_name}/${app_ver}/maven-metadata.xml"
        echo "metadata_url=${metadata_url}"
        local app_snap_ver=$(curl -s ${metadata_url} | xmllint --xpath "//snapshotVersion[1]/value/text()" -)
        local snap_timestamp=$(curl -s ${metadata_url} | xmllint --xpath "//timestamp/text()" -)
        local snap_buildNumber=$(curl -s ${metadata_url} | xmllint --xpath "//buildNumber/text()" -)
        echo "app_snap_ver=${app_snap_ver}"
        echo "snap_timestamp=${snap_timestamp},snap_buildNumber=${snap_buildNumber}"
        
        local download_url="${nexus_url}/mgkim/service/${app_name}/${app_ver}/${app_name}-${app_snap_ver}.jar"
        jar_file="${app_name}-${app_ver}-${snap_timestamp}-${snap_buildNumber}.jar"
        echo "download_url=${download_url} to jar_file=${jar_file}"
        ;;
      @(s)*)
        # lastest artifact (maven-release)
        local nexus_url="https://nexus/repository/maven-release"
        metadata_url="${nexus_url}/mgkim/service/${app_name}/maven-metadata.xml"
        echo "metadata_url=${metadata_url}"
        app_ver=$(curl -s ${metadata_url} | xmllint --xpath "//version[last()]/text()" -)
        echo "app_ver=${app_ver}"
        
        local download_url="${nexus_url}/mgkim/service/${app_name}/${app_ver}/${app_name}-${app_ver}.jar"
        jar_file="${app_name}-${app_ver}.jar"
        echo "download_url=${download_url} to jar_file=${jar_file}"
        ;;
    esac
    
    # download
    download_cmd="curl --silent --output ${BASEDIR}/${jar_file} ${download_url}"
    echo "download_cmd=${download_cmd}"
    if [ $(whoami) == "root" ]; then
      su ${EXEC_USER} -c "${download_cmd}"
    elif [ $(whoami) == ${EXEC_USER} ]; then
      eval "${download_cmd}"
    else
      echo "current user "$(whoami)
      exit -1
    fi
    
    # final_name
    local md5str=$(md5sum ${BASEDIR}/${jar_file} | awk '{ print substr($1, 1, 4) }')
    local final_name=${jar_file%.*}_${md5str}.${jar_file##*.}
    local mv_cmd="mv ${BASEDIR}/${jar_file} ${BASEDIR}/${final_name}"
    echo "mv_cmd=${mv_cmd}"
    if [ $(whoami) == "root" ]; then
      su ${EXEC_USER} -c "${mv_cmd}"
    elif [ $(whoami) == ${EXEC_USER} ]; then
      eval "${mv_cmd}"
    else
      echo "current user "$(whoami)
      exit -1
    fi
    echo "final_name=${final_name}"
    
    
    # stop / start
    local app_id_arr
    read -ra app_id_arr <<< $(GetSvrInfo "app_id" "profile_sys" "${1:0:1}" "app_name" "${app_name}")
    
    for app_id in ${app_id_arr[@]}
    do
      if [ $(whoami) == "root" ]; then
        su ${EXEC_USER} -c "${BASEDIR}/stop.sh ${app_id}"
        su ${EXEC_USER} -c "${BASEDIR}/start.sh ${app_id} ${final_name}"
      elif [ $(whoami) == ${EXEC_USER} ]; then
        ${BASEDIR}/stop.sh ${app_id}
        ${BASEDIR}/start.sh ${app_id} ${final_name}
      else
        echo "current user "$(whoami)
        exit -1
      fi
    done
  done
  
  echo "--- //(deploy) deploy ---"
}



case "$1" in
  @(d|s)?(ev|ta))
    ;;
  @(d|s)@(w|a)?(ww|dm))
    ;;
  *)
    echo "Usage: ${0##*/} [all|d|s|w|a|dw|da|sw|sa]"
    exit 0;
    ;;
esac

deploy "$1";

echo "### [finish] ${0##*/} ${@} ###"$'\n'$'\n'
