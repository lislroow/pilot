#!/bin/bash

echo "### [start] ${0##*/} ${@} ###"

## env
echo "+++ (system-env) +++"
BASEDIR="$( cd $( dirname "$0" ) && pwd -P)"

## include
. ${BASEDIR}/script/include.sh


function send_conf() {
  echo "+++ ($FUNCNAME) transfer logback.xml, application.yaml +++"
  
  case "$1" in
    all)
      # sd , aw > ip
      read -ra app_name_arr <<< $(GetSvrInfo "app_name" "ALL")
      read -ra profile_sys_arr <<< $(GetSvrInfo "profile_sys" "ALL")
      
      for profile_sys in ${profile_sys_arr[@]}
      do
        for app_name in ${app_name_arr[@]}
        do
          read -r  app_home <<< $(GetSvrInfo "app_home" "profile_sys" "${profile_sys}" "app_name" "${app_name}")
          read -ra ip_arr <<< $(GetSvrInfo "ip" "profile_sys" "${profile_sys}" "app_name" "${app_name}")
          echo "ip_arr=${ip_arr[@]}, cnt=${#ip_arr[@]}"
          
          for ip in ${ip_arr[@]}
          do
            ## [actual-code]
            local files=("${BASEDIR}/${app_name}/src/main/resources/.logback-${app_name##*-}-${app_home##*-}.xml")
            echo "files=${files[@]}"
            local scp_cmd="scp ${files[@]} ${EXEC_USER}@${ip}:${app_home}"
            echo "scp_cmd=${scp_cmd}"
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
        read -r  app_home <<< $(GetSvrInfo "app_home" "profile_sys" "${profile_sys}" "app_name" "${app_name}")
        read -ra ip_arr <<< $(GetSvrInfo "ip" "profile_sys" "${profile_sys}" "app_name" "${app_name}")
        echo "ip_arr=${ip_arr[@]}, cnt=${#ip_arr[@]}"
        
        for ip in ${ip_arr[@]}
        do
          ## [actual-code]
          local files=("${BASEDIR}/${app_name}/src/main/resources/.logback-${app_name##*-}-${app_home##*-}.xml")
          echo "files=${files[@]}"
          local scp_cmd="scp ${files[@]} ${EXEC_USER}@${ip}:${app_home}"
          echo "scp_cmd=${scp_cmd}"
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
        read -r  app_home <<< $(GetSvrInfo "app_home" "profile_sys" "${profile_sys}" "app_name" "${app_name}")
        read -ra ip_arr <<< $(GetSvrInfo "ip" "profile_sys" "${profile_sys}" "app_name" "${app_name}")
        echo "ip_arr=${ip_arr[@]}, cnt=${#ip_arr[@]}"
        
        for ip in ${ip_arr[@]}
        do
          ## [actual-code]
          local files=("${BASEDIR}/${app_name}/src/main/resources/.logback-${app_name##*-}-${app_home##*-}.xml")
          echo "files=${files[@]}"
          local scp_cmd="scp ${files[@]} ${EXEC_USER}@${ip}:${app_home}"
          echo "scp_cmd=${scp_cmd}"
          eval "${scp_cmd}"
          ## //[actual-code]
        done
      done
      ;;
    @(d|s)@(w|a)?(ww|dm))
      # dw > d, w > ip
      read -r  app_name <<< $(GetSvrInfo "app_name" "app_id" "$1")
      read -r  profile_sys <<< $(GetSvrInfo "profile_sys" "app_id" "$1")
      read -r  app_home <<< $(GetSvrInfo "app_home" "profile_sys" "${profile_sys}" "app_name" "${app_name}")
      read -ra ip_arr <<< $(GetSvrInfo "ip" "profile_sys" "${profile_sys}" "app_name" "${app_name}")
      echo "ip_arr=${ip_arr[@]}, cnt=${#ip_arr[@]}"
      
      for ip in ${ip_arr[@]}
      do
        ## [actual-code]
        local files=("${BASEDIR}/${app_name}/src/main/resources/.logback-${app_name##*-}-${app_home##*-}.xml")
        echo "files=${files[@]}"
        local scp_cmd="scp ${files[@]} ${EXEC_USER}@${ip}:${app_home}"
        echo "scp_cmd=${scp_cmd}"
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
    exit 0;
    ;;
esac

send_conf "$1";

echo "### [finish] ${0##*/} ${@} ###"$'\n'$'\n'
