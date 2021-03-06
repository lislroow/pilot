#!/bin/bash

## env
BASEDIR="$( cd $( dirname "$0" ) && pwd -P)"
echo -e "\e[35m+++ [file] ${BASEDIR}/${0##*/} ${@} +++\e[m"
. ${BASEDIR}/sh/include.sh

verboss="false"

function publish() {
  echo "+++ [func] ${BASEDIR}/${0##*/}:$FUNCNAME: deploying trigger +++"
  
  case "$1" in
    all)
      # sd , aw > ip
      read -ra app_name_arr <<< $(GetSvrInfo "app_name" "ALL")
      read -ra profile_sys_arr <<< $(GetSvrInfo "profile_sys" "ALL")
      tot=$((${#profile_sys_arr[@]} + ${#app_name_arr[@]}))
      idx=1
      for profile_sys in ${profile_sys_arr[@]}
      do
        for app_name in ${app_name_arr[@]}
        do
          read -r  app_home <<< $(GetSvrInfo "app_home" "profile_sys" "${profile_sys}" "app_name" "${app_name}")
          read -ra ip_arr <<< $(GetSvrInfo "ip" "profile_sys" "${profile_sys}" "app_name" "${app_name}")
          Log $verboss "ip_arr=${ip_arr[@]}, cnt=${#ip_arr[@]}"
          
          for ip in ${ip_arr[@]}
          do
            ## [actual-code]
            local ssh_cmd="ssh ${EXEC_USER}@${ip} '${app_home}/deploy.sh' ${profile_sys:0:1}${app_name:(${#DOMAIN}+1):1}"
            echo -e "   ssh(${app_name}): \e[30;42m${ssh_cmd}\e[m"
            eval "${ssh_cmd}"
            ## //[actual-code]
          done
          idx=$(( $idx + 1 ))
        done
      done
      ;;
    @(d|s)?(ev|ta))
      # s > aw > ip
      read -r  profile_sys <<< $(GetSvrInfo "profile_sys" "profile_sys" "$1")
      read -ra app_name_arr <<< $(GetSvrInfo "app_name" "profile_sys" "$1")
      tot="${#app_name_arr[@]}"
      idx=1
      for app_name in ${app_name_arr[@]}
      do
        read -r  app_home <<< $(GetSvrInfo "app_home" "profile_sys" "${profile_sys}" "app_name" "${app_name}")
        read -ra ip_arr <<< $(GetSvrInfo "ip" "profile_sys" "${profile_sys}" "app_name" "${app_name}")
        Log $verboss "ip_arr=${ip_arr[@]}, cnt=${#ip_arr[@]}"
        
        for ip in ${ip_arr[@]}
        do
          ## [actual-code]
          local ssh_cmd="ssh ${EXEC_USER}@${ip} '${app_home}/deploy.sh ${profile_sys:0:1}${app_name:(${#DOMAIN}+1):1}'"
          echo -e "## \e[36mssh:\e[m ${ssh_cmd}"
          echo -e "   ssh(${app_name}): \e[30;42m${ssh_cmd}\e[m"
          eval "${ssh_cmd}"
          ## //[actual-code]
        done
        idx=$(( $idx + 1 ))
      done
      ;;
    @(w|a)?(ww|dm))
      # w > ds > ip
      read -r  app_name <<< $(GetSvrInfo "app_name" "app_name" "$1")
      read -ra profile_sys_arr <<< $(GetSvrInfo "profile_sys" "app_name" "$1")
      tot="${#profile_sys_arr[@]}"
      idx=1
      for profile_sys in ${profile_sys_arr[@]}
      do
        read -r  app_home <<< $(GetSvrInfo "app_home" "profile_sys" "${profile_sys}" "app_name" "${app_name}")
        read -ra ip_arr <<< $(GetSvrInfo "ip" "profile_sys" "${profile_sys}" "app_name" "${app_name}")
        Log $verboss "ip_arr=${ip_arr[@]}, cnt=${#ip_arr[@]}"
        
        for ip in ${ip_arr[@]}
        do
          ## [actual-code]
          local ssh_cmd="ssh ${EXEC_USER}@${ip} '${app_home}/deploy.sh ${profile_sys:0:1}${app_name:(${#DOMAIN}+1):1}'"
          echo -e "   ssh(${app_name}): \e[30;42m${ssh_cmd}\e[m"
          eval "${ssh_cmd}"
          ## //[actual-code]
        done
        idx=$(( $idx + 1 ))
      done
      ;;
    @(d|s)@(w|a)?(ww|dm))
      # dw > d, w > ip
      read -r  app_name <<< $(GetSvrInfo "app_name" "app_id" "$1")
      read -r  profile_sys <<< $(GetSvrInfo "profile_sys" "app_id" "$1")
      read -r  app_home <<< $(GetSvrInfo "app_home" "profile_sys" "${profile_sys}" "app_name" "${app_name}")
      read -ra ip_arr <<< $(GetSvrInfo "ip" "profile_sys" "${profile_sys}" "app_name" "${app_name}")
      Log $verboss "ip_arr=${ip_arr[@]}, cnt=${#ip_arr[@]}"
      
      for ip in ${ip_arr[@]}
      do
        ## [actual-code]
        local ssh_cmd="ssh ${EXEC_USER}@${ip} '${app_home}/deploy.sh ${profile_sys:0:1}${app_name:(${#DOMAIN}+1):1}'"
        echo -e "   ssh(${app_name}): \e[30;42m${ssh_cmd}\e[m"
        eval "${ssh_cmd}"
        ## //[actual-code]
      done
      ;;
  esac
}



case "$1" in
  all)
    ;;
  @(d|s)?(ev|ta))
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



publish "$1";


