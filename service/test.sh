#!/bin/bash

echo "### [file] ${0##*/} ${@} ###"

## include
. ./sh/include.sh

DOMAIN="pilot"
profile_sys="dev"
app_name="pilot-www"
#echo "${profile_sys:0:1}"
#echo "${app_name:(${#DOMAIN}+1):1}"

read -ra  app_name <<< $(GetSvrInfo "app_name" "app_id" "d")
#echo "${#app_name[@]}"
#echo "${app_name[0]}"

#case "$1" in
#  all)
#    ;;
#  @(d|s)@(w|a)?(ww|dm)@(1|2)@(1|2))
#    echo "$1"
#    ;;
#  *)
#    echo "Usage: ${0##*/} [dw1|dw2|da1|da2|sw1|sw2|sa1|sa2]"
#    exit -1
#    ;;
#esac

#if [ "$1" == "all" ] && [ "$2" != "" ]; then
#  echo "true"
#fi

#if [ "${OS_NAME}" == "linux" ]; then
#  LOCAL_IP=$(ip -4 addr show ens33 | grep -oP '(?<=inet\s)\d+(\.\d+){3}')
#else
#  LOCAL_IP="localhost"
#fi
#read -ra app_id_arr <<< $(GetSvrInfo "app_id" "profile_sys" "dev" "ip" "${LOCAL_IP}")
#echo "app_id_arr=${app_id_arr[@]}"


#echo "${1:0:1}"

