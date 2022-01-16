#!/bin/bash

echo "### [start] ${0##*/} ${@} ###"

## env
echo "+++ (system-env) +++"
BASEDIR="$( cd $( dirname "$0" ) && pwd -P)"

## include
. ${BASEDIR}/script/include.sh

## (send_script) transfer *.sh files
function send_script() {
  echo "+++ (send_script) transfer *.sh files +++"
  files=(
    "${BASEDIR}/script/*.sh"
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
  
  echo "--- //(send_script) transfer *.sh files ---"
}



case "$1" in
  all)
    ;;
  d?(ev)|s?(ta))
    ;;
  *)
    echo "Usage: ${0##*/} [all|d|s]"
    exit 0;
    ;;
esac

send_script "$1";

echo "### [finish] ${0##*/} ${@} ###"$'\n'$'\n'
