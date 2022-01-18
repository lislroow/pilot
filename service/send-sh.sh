#!/bin/bash

## env
BASEDIR="$( cd $( dirname "$0" ) && pwd -P)"
echo -e "\e[35m+++ [file] ${BASEDIR}/${0##*/} ${@} +++\e[m"
. ${BASEDIR}/sh/include.sh

verboss="false"

function send_sh() {
  echo "+++ [func] ${BASEDIR}/${0##*/}:$FUNCNAME: transfer *.sh files +++"
  
  files=(
    "${BASEDIR}/sh/*.sh"
  )
  Log $verboss "files=${files[@]}"
  
  case "$1" in
    all)
      read -ra profile_sys_arr <<< $(GetSvrInfo "profile_sys" "ALL")
      
      for profile_sys in ${profile_sys_arr[@]}
      do
        read -r  app_home <<< $(GetSvrInfo "app_home" "profile_sys" "${profile_sys}")
        read -ra ip_arr <<< $(GetSvrInfo "ip" "profile_sys" "${profile_sys}")
        Log $verboss "ip_arr=${ip_arr[@]}, cnt=${#ip_arr[@]}"
        
        for ip in ${ip_arr[@]}
        do
          ## [actual-code]
          local scp_cmd="scp ${files[@]} ${EXEC_USER}@${ip}:${app_home}"
          echo -e "## \e[30;42m${scp_cmd}\e[m"
          eval "${scp_cmd}"
          local ssh_cmd="ssh ${EXEC_USER}@${ip} 'chmod u+x ${app_home}/*.sh'"
          echo -e "## \e[30;42m${ssh_cmd}\e[m"
          eval "${ssh_cmd}"
          ## //[actual-code]
        done
      done
      ;;
    d?(ev)|s?(ta))
      read -r  profile_sys <<< $(GetSvrInfo "profile_sys" "profile_sys" "$1")
      read -r  app_home <<< $(GetSvrInfo "app_home" "profile_sys" "${profile_sys}")
      read -ra ip_arr <<< $(GetSvrInfo "ip" "profile_sys" "${profile_sys}")
      Log $verboss "ip_arr=${ip_arr[@]}, cnt=${#ip_arr[@]}"
      
      for ip in ${ip_arr[@]}
      do
        ## [actual-code]
        local scp_cmd="scp ${files[@]} ${EXEC_USER}@${ip}:${app_home}"
        echo -e "## \e[30;42m${scp_cmd}\e[m"
        eval "${scp_cmd}"
        local ssh_cmd="ssh ${EXEC_USER}@${ip} 'chmod u+x ${app_home}/*.sh'"
        echo -e "## \e[30;42m${ssh_cmd}\e[m"
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

