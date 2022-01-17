#!/bin/bash


echo "### [start] ${0##*/} ${@} ###"

## env
echo "+++ (system-env) +++"
BASEDIR="$( cd $( dirname "$0" ) && pwd -P)"

## include
. ${BASEDIR}/include.sh


function execute() {
  echo "+++ ($FUNCNAME) +++"
  
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
  echo "app_id_arr=${app_id_arr[@]}"
  
  curl_user_idlogin "${app_id_arr[@]}"
  curl_apitxlog_selectLogList "${app_id_arr[@]}"
}

function curl_template() {
  echo "+++ ($FUNCNAME) +++"
  local app_id_arr=($@)
  local tot=${#app_id_arr[@]}
  local idx=1
  for app_id in ${app_id_arr[@]}
  do
    read -r  port <<< $(GetSvrInfo "port" "app_id" "${app_id}")
    echo "[${idx}/${tot}] app_id=${app_id} port=${port}"
    ## [actual-code]
    response=$("paste here")
    # (example) instruction !!
    #   1) paste here
    #   2) add option: --silent
    #   3) replace url: http://localhost:'${port}'
    #response=$(curl --location --request POST --silent 'http://localhost:'${port}'/public/cmm/user/idlogin' \
    ## //[actual-code]
    echo "response=${response}"
    idx=$(( $idx + 1 ))
  done
}

function curl_user_idlogin() {
  echo "+++ ($FUNCNAME) +++"
  local app_id_arr=($@)
  local tot=${#app_id_arr[@]}
  local idx=1
  for app_id in ${app_id_arr[@]}
  do
    read -r  port <<< $(GetSvrInfo "port" "app_id" "${app_id}")
    echo "[${idx}/${tot}] app_id=${app_id} port=${port}"
    ## [actual-code]
    response=$(curl --location --request POST --silent 'http://localhost:'${port}'/public/cmm/user/idlogin' \
    --header 'debug: Y' \
    --header 'Content-Type: application/json' \
    --data-raw '{
      "header": {},
      "body": {
        "userId": "1000000001"
      }
    }')
    ## //[actual-code]
    echo "response=${response}"
    idx=$(( $idx + 1 ))
  done
}

function curl_apitxlog_selectLogList() {
  echo "+++ ($FUNCNAME) +++"
  local app_id_arr=($@)
  local tot=${#app_id_arr[@]}
  local idx=1
  for app_id in ${app_id_arr[@]}
  do
    read -r  port <<< $(GetSvrInfo "port" "app_id" "${app_id}")
    echo "[${idx}/${tot}] app_id=${app_id} port=${port}"
    ## [actual-code]
    response=$(curl --location --request POST --silent 'http://localhost:'${port}'/api/adm/apitxlog/selectLogList' \
    --header 'Authorization: Bearer eyJ0eXAiOiJhY2Nlc3NUb2tlbiIsImV4cCI6MTY0MDIzMDQ4MDg1MywiYWxnIjoiSFMyNTYifQ.eyJ1c2VyVHBjZCI6ImFwaSIsInNzdmFsZFNlYyI6MTAwMDAwMDAsImF1bXRoVHBjZCI6IjAxIiwic2l0ZVRwY2QiOiIxMCIsInVzZXJJZCI6IjEwMDAwMDAwMDEiLCJzc2lkIjoidG9nNXdmYjR4azUwMCJ9.craIp4TKqIXR9EsppMPL6sAQz8JmvA17KFf0b0QDsAo' \
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
    echo "response=${response}"
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

echo "### [finish] ${0##*/} ${@} ###"$'\n'$'\n'
