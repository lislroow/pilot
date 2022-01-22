#!/bin/bash

## env
BASEDIR="$( cd $( dirname "$0" ) && pwd -P)"
echo -e "\e[35m+++ [file] ${BASEDIR}/${0##*/} ${@} +++\e[m"
. ${BASEDIR}/include.sh

BEARER_TOKEN="eyJ0eXAiOiJhY2Nlc3NUb2tlbiIsImV4cCI6MTY0MjQ4MjMwNDUwNiwiYWxnIjoiSFMyNTYifQ.eyJ1c2VyVHBjZCI6ImFwaSIsInNzdmFsZFNlYyI6MTAwMDAwMDAsImF1bXRoVHBjZCI6IjAxIiwiYXBwQ2QiOiIxMCIsInVzZXJJZCI6IjEwMDAwMDAwMDEiLCJzc2lkIjoiczFqZXRiamxvMGtweDAifQ.S_JoCx0CSuw_gywbdOCRM34SH0WnIPpQp8eubq2Zxsw"
CURL_OPTS="--connect-timeout 10 --silent"

function execute() {
  echo "+++ [func] ${BASEDIR}/${0##*/}:$FUNCNAME +++"
  
  local app_id_arr
  case "$1" in
    all)
      read -ra app_id_arr <<< $(GetSvrInfo "app_id" "ALL")
      ;;
    @(d|s)?(ev|ta))
      read -ra app_id_arr <<< $(GetSvrInfo "app_id" "profile_sys" "$1")
      ;;
    @(w|a)?(ww|dm))
      read -ra app_id_arr <<< $(GetSvrInfo "app_id" "app_name" "$1")
      ;;
    @(d|s)@(w|a)*)
      read -ra app_id_arr <<< $(GetSvrInfo "app_id" "app_id" "$1")
      ;;
  esac
  
  echo -e "## \e[36mtarget(${#app_id_arr[@]}):\e[m ${app_id_arr[@]}"
  curl_user_idlogin "${app_id_arr[@]}"
  
  echo $'\n-----------\n'
  
  echo -e "## \e[36mtarget(${#app_id_arr[@]}):\e[m ${app_id_arr[@]}"
  curl_apitxlog_selectLogList "${app_id_arr[@]}"
}

function curl_template() {
  echo "+++ [func] ${BASEDIR}/${0##*/}:$FUNCNAME +++"
  local app_id_arr=($@)
  local tot=${#app_id_arr[@]}
  local idx=1
  for app_id in ${app_id_arr[@]}
  do
    read -r  port <<< $(GetSvrInfo "port" "app_id" "${app_id}")
    echo -e "## \e[36m[${idx}/${tot}] app_id=${app_id} port=${port}\e[m"
    ## [actual-code]
    response=$("paste here")
    # (example) instruction !!
    #   1) paste here
    #   2) add option: ${CURL_OPTS}
    #   3) replace url: http://'${ip}':'${port}'
    #      response=$(curl --location --request POST ${CURL_OPTS} 'http://'${ip}':'${port}'/public/cmm/user/idlogin' \
    #   4) replace authorization: 
    #      --header 'Authorization: Bearer '${BEARER_TOKEN} \
    ## //[actual-code]
    echo "   ${response}"
    idx=$(( $idx + 1 ))
  done
}

function curl_user_idlogin() {
  echo "+++ [func] ${BASEDIR}/${0##*/}:$FUNCNAME +++"
  local app_id_arr=($@)
  local tot=${#app_id_arr[@]}
  local idx=1
  for app_id in ${app_id_arr[@]}
  do
    read -r  port <<< $(GetSvrInfo "port" "app_id" "${app_id}")
    read -r  ip <<< $(GetSvrInfo "ip" "app_id" "${app_id}")
    echo -e "## \e[36m[${idx}/${tot}] app_id=${app_id} port=${port}\e[m"
    ## [actual-code]
    response=$(curl --location --request POST ${CURL_OPTS} 'http://'${ip}':'${port}'/public/cmm/user/idlogin' \
    --header 'debug: Y' \
    --header 'Content-Type: application/json' \
    --data-raw '{
      "header": {},
      "body": {
        "userId": "1000000001"
      }
    }')
    ## //[actual-code]
    echo "   ${response}"
    idx=$(( $idx + 1 ))
  done
}

function curl_apitxlog_selectLogList() {
  echo "+++ [func] ${BASEDIR}/${0##*/}:$FUNCNAME +++"
  local app_id_arr=($@)
  local tot=${#app_id_arr[@]}
  local idx=1
  for app_id in ${app_id_arr[@]}
  do
    read -r  port <<< $(GetSvrInfo "port" "app_id" "${app_id}")
    echo -e "## \e[36m[${idx}/${tot}] app_id=${app_id} port=${port}\e[m"
    ## [actual-code]
    response=$(curl --location --request POST ${CURL_OPTS} 'http://'${ip}':'${port}'/api/com/apitxlog/selectLogList' \
    --header 'Authorization: Bearer '${BEARER_TOKEN} \
    --header 'Content-Type: application/json' \
    --data-raw '{
      "body": {
      },
      "header": {
        "debug": "Y"
      },
      "page": {
        "pageindex": 1,
        "pageunit": 4,
        "paging": true,
        "rowunit": 4
      }
    }')
    ## //[actual-code]
    echo "   ${response}"
    idx=$(( $idx + 1 ))
  done
}

case "$1" in
  all)
    ;;
  @(d|s)?(ev|ta))
    ;;
  @(w|a)?(ww|dm))
    ;;
  @(d|s)@(w|a)*)
    ;;
  *)
    echo "Usage: ${0##*/} [all|\${profile_sys}|\${app_name_c3}|\${app_id}]"
    execute "${PROFILE_SYS}";
    exit 0;
    ;;
esac


execute "$1";

