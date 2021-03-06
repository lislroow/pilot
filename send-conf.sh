#!/bin/bash

## env
BASEDIR="$( cd $( dirname "$0" ) && pwd -P)"
echo -e "\e[35m+++ [file] ${BASEDIR}/${0##*/} ${@} +++\e[m"
. ${BASEDIR}/sh/include.sh

verboss="false"

function send_conf() {
  echo "+++ [func] ${BASEDIR}/${0##*/}:$FUNCNAME: transfer logback.xml, application.yaml +++"
  
  case "$1" in
    all)
      # sd , aw > ip
      read -ra app_name_arr <<< $(GetSvrInfo "app_name" "ALL")
      read -ra profile_sys_arr <<< $(GetSvrInfo "profile_sys" "ALL")
      
      for profile_sys in ${profile_sys_arr[@]}
      do
        for app_name in ${app_name_arr[@]}
        do
          read -r  app_group <<< $(GetSvrInfo "app_group" "app_name" "${app_name}")
          read -r  app_home <<< $(GetSvrInfo "app_home" "profile_sys" "${profile_sys}" "app_name" "${app_name}")
          read -ra ip_arr <<< $(GetSvrInfo "ip" "profile_sys" "${profile_sys}" "app_name" "${app_name}")
          Log $verboss "ip_arr=${ip_arr[@]}, cnt=${#ip_arr[@]}"
          
          for ip in ${ip_arr[@]}
          do
            ## [actual-code]
            #local files=("${BASEDIR}/${app_group}/${app_name}/src/main/resources/.logback-${app_name##*-}-${app_home##*-}.xml")
            local files=("${BASEDIR}/${app_group}/${app_name}/src/main/resources/logback.xml")
            Log $verboss "files=${files[@]}"
            #local scp_cmd="scp ${files[@]} ${EXEC_USER}@${ip}:${app_home}"
            local scp_cmd="scp ${files[@]} ${EXEC_USER}@${ip}:${app_home}/.logback-${app_name##*-}-${app_home##*-}.xml"
            echo -e "## \e[30;42m${scp_cmd}\e[m"
            eval "${scp_cmd}"
            ## //[actual-code]
          done
        done
      done
      ;;
    d?(ev)|s?(ta))
      # s > aw > ip
      read -r  profile_sys <<< $(GetSvrInfo "profile_sys" "profile_sys" "$1")
      read -ra app_name_arr <<< $(GetSvrInfo "app_name" "profile_sys" "$1")
      for app_name in ${app_name_arr[@]}
      do
        read -r  app_group <<< $(GetSvrInfo "app_group" "app_name" "${app_name}")
        read -r  app_home <<< $(GetSvrInfo "app_home" "profile_sys" "${profile_sys}" "app_name" "${app_name}")
        read -ra ip_arr <<< $(GetSvrInfo "ip" "profile_sys" "${profile_sys}" "app_name" "${app_name}")
        Log $verboss "ip_arr=${ip_arr[@]}, cnt=${#ip_arr[@]}"
        
        for ip in ${ip_arr[@]}
        do
          ## [actual-code]
          #local files=("${BASEDIR}/${app_group}/${app_name}/src/main/resources/.logback-${app_name##*-}-${app_home##*-}.xml")
          local files=("${BASEDIR}/${app_group}/${app_name}/src/main/resources/logback.xml")
          Log $verboss "files=${files[@]}"
          #local scp_cmd="scp ${files[@]} ${EXEC_USER}@${ip}:${app_home}"
          local scp_cmd="scp ${files[@]} ${EXEC_USER}@${ip}:${app_home}/.logback-${app_name##*-}-${app_home##*-}.xml"
          echo -e "## \e[30;42m${scp_cmd}\e[m"
          eval "${scp_cmd}"
          ## //[actual-code]
        done
      done
      ;;
    @(w|a)?(ww|dm))
      # w > ds > ip
      read -r  app_name <<< $(GetSvrInfo "app_name" "app_name" "$1")
      read -ra profile_sys_arr <<< $(GetSvrInfo "profile_sys" "app_name" "$1")
      for profile_sys in ${profile_sys_arr[@]}
      do
        read -r  app_group <<< $(GetSvrInfo "app_group" "app_name" "${app_name}")
        read -r  app_home <<< $(GetSvrInfo "app_home" "profile_sys" "${profile_sys}" "app_name" "${app_name}")
        read -ra ip_arr <<< $(GetSvrInfo "ip" "profile_sys" "${profile_sys}" "app_name" "${app_name}")
        Log $verboss "ip_arr=${ip_arr[@]}, cnt=${#ip_arr[@]}"
        
        for ip in ${ip_arr[@]}
        do
          ## [actual-code]
          #local files=("${BASEDIR}/${app_group}/${app_name}/src/main/resources/.logback-${app_name##*-}-${app_home##*-}.xml")
          local files=("${BASEDIR}/${app_group}/${app_name}/src/main/resources/logback.xml")
          Log $verboss "files=${files[@]}"
          #local scp_cmd="scp ${files[@]} ${EXEC_USER}@${ip}:${app_home}"
          local scp_cmd="scp ${files[@]} ${EXEC_USER}@${ip}:${app_home}/.logback-${app_name##*-}-${app_home##*-}.xml"
          echo -e "## \e[30;42m${scp_cmd}\e[m"
          eval "${scp_cmd}"
          ## //[actual-code]
        done
      done
      ;;
    @(d|s)@(w|a)?(ww|dm))
      # dw > d, w > ip
      read -r  app_name <<< $(GetSvrInfo "app_name" "app_id" "$1")
      read -r  app_group <<< $(GetSvrInfo "app_group" "app_name" "${app_name}")
      read -r  profile_sys <<< $(GetSvrInfo "profile_sys" "app_id" "$1")
      read -r  app_home <<< $(GetSvrInfo "app_home" "profile_sys" "${profile_sys}" "app_name" "${app_name}")
      read -ra ip_arr <<< $(GetSvrInfo "ip" "profile_sys" "${profile_sys}" "app_name" "${app_name}")
      Log $verboss "ip_arr=${ip_arr[@]}, cnt=${#ip_arr[@]}"
      
      for ip in ${ip_arr[@]}
      do
        ## [actual-code]
        #local files=("${BASEDIR}/${app_group}/${app_name}/src/main/resources/.logback-${app_name##*-}-${app_home##*-}.xml")
        local files=("${BASEDIR}/${app_group}/${app_name}/src/main/resources/logback.xml")
        Log $verboss "files=${files[@]}"
        #local scp_cmd="scp ${files[@]} ${EXEC_USER}@${ip}:${app_home}"
        local scp_cmd="scp ${files[@]} ${EXEC_USER}@${ip}:${app_home}/.logback-${app_name##*-}-${app_home##*-}.xml"
        echo -e "## \e[30;42m${scp_cmd}\e[m"
        eval "${scp_cmd}"
        ## //[actual-code]
      done
      ;;
  esac
}



case "$1" in
  all)
    ;;
  d?(ev)|s?(ta))
    ;;
  @(w|a)?(ww|dm))
    ;;
  @(d|s)@(w|a)?(ww|dm))
    ;;
  *)
    echo "Usage: ${0##*/} [all|d|s|w|a|dw|da|sw|sa]"
    send_conf "all";
    exit 0;
    ;;
esac

send_conf "$1";

