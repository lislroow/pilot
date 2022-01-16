#!/bin/bash

echo "### [start] ${0##*/} ${@} ###"

## include
. ./script/include.sh

## env
echo "+++ (system-env) +++"
BASEDIR="$( cd $( dirname "$0" ) && pwd -P)"

UNAME=`uname -s`
if [[ "${UNAME}" = "Linux"* ]]; then
  OS_NAME="linux"
elif [[ "${UNAME}" = "CYGWIN"* || "${UNAME}" = "MINGW"* ]]; then
  OS_NAME="win"
fi

printf '%s\n' $(cat << EOF
BASEDIR=${BASEDIR}
UNAME=${UNAME}
OS_NAME=${OS_NAME}
EOF
)




## (publish) deploying trigger
function publish() {
  echo "+++ (publish) deploying trigger +++"
  
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
            ssh ${EXEC_USER}@${ip} "${app_home}/deploy.sh" ${profile_sys} ${app_name}
          done
        done
      done
      ;;
    d|s)
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
          ssh ${EXEC_USER}@${ip} "${app_home}/deploy.sh" ${profile_sys} ${app_name}
        done
      done
      ;;
    w|a)
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
          ssh ${EXEC_USER}@${ip} "${app_home}/deploy.sh" ${profile_sys} ${app_name}
        done
      done
      ;;
    dw*|sw*|da*|sa*)
      # dw > d, w > ip
      read -r  app_name <<< $(GetSvrInfo "app_name" "app_id" "$1")
      read -r  profile_sys <<< $(GetSvrInfo "profile_sys" "app_id" "$1")
      read -r  app_home <<< $(GetSvrInfo "app_home" "profile_sys" "${profile_sys}" "app_name" "${app_name}")
      read -ra ip_arr <<< $(GetSvrInfo "ip" "profile_sys" "${profile_sys}" "app_name" "${app_name}")
      echo "ip_arr=${ip_arr[@]}, cnt=${#ip_arr[@]}"
      
      for ip in ${ip_arr[@]}
      do
        ssh ${EXEC_USER}@${ip} "${app_home}/deploy.sh" ${profile_sys} ${app_name}
      done
      ;;
  esac
  
  echo "--- //(publish) deploying trigger ---"
}


echo "+++ (runtime-env) +++"
EXEC_USER="tomcat"
case "$1" in
  all)
    ;;
  d|s)
    ;;
  w|a)
    ;;
  dw|sw|da|sa)
    ;;
  *)
    echo "Usage: ${0##*/} [all|d|s|w|a|dw|da|sw|sa]"
    exit 0;
    ;;
esac



publish "$1";


echo "### [finish] ${0##*/} ${@} ###"$'\n'$'\n'
