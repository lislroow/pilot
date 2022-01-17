#!/bin/bash

## env
BASEDIR="$( cd $( dirname "$0" ) && pwd -P)"
echo -e "\e[35m+++ [file] ${BASEDIR}/${0##*/} ${@} +++\e[m"
. ${BASEDIR}/sh/include.sh


function send_sh() {
  echo "+++ [func] ${BASEDIR}/${0##*/}:$FUNCNAME: transfer *.sh files +++"
  
  files=(
    "${BASEDIR}/sh/*.sh"
  )
  echo "files=${files[@]}"
  
  case "$1" in
    all)
      read -ra profile_sys_arr <<< $(GetSvrInfo "profile_sys" "ALL")
      
      for profile_sys in ${profile_sys_arr[@]}
      do
        read -r  app_home <<< $(GetSvrInfo "app_home" "profile_sys" "${profile_sys}")
        read -ra ip_arr <<< $(GetSvrInfo "ip" "profile_sys" "${profile_sys}")
        echo "ip_arr=${ip_arr[@]}, cnt=${#ip_arr[@]}"
        
        for ip in ${ip_arr[@]}
        do
          ## [actual-code]
          local scp_cmd="scp ${files[@]} ${EXEC_USER}@${ip}:${app_home}"
          echo "scp_cmd=${scp_cmd}"
          eval "${scp_cmd}"
          local ssh_cmd="ssh ${EXEC_USER}@${ip} 'chmod u+x ${app_home}/*.sh'"
          echo "ssh_cmd=${ssh_cmd}"
          eval "${ssh_cmd}"
          ## //[actual-code]
        done
      done
      ;;
    d?(ev)|s?(ta))
      read -r  profile_sys <<< $(GetSvrInfo "profile_sys" "profile_sys" "$1")
      read -r  app_home <<< $(GetSvrInfo "app_home" "profile_sys" "${profile_sys}")
      read -ra ip_arr <<< $(GetSvrInfo "ip" "profile_sys" "${profile_sys}")
      echo "ip_arr=${ip_arr[@]}, cnt=${#ip_arr[@]}"
      
      for ip in ${ip_arr[@]}
      do
        ## [actual-code]
        local scp_cmd="scp ${files[@]} ${EXEC_USER}@${ip}:${app_home}"
        echo "scp_cmd=${scp_cmd}"
        eval "${scp_cmd}"
        local ssh_cmd="ssh ${EXEC_USER}@${ip} 'chmod u+x ${app_home}/*.sh'"
        echo "ssh_cmd=${ssh_cmd}"
        eval "${ssh_cmd}"
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
  *)
    echo "Usage: ${0##*/} [all|d|s]"
    send_sh "all";
    exit 0;
    ;;
esac

send_sh "$1";

