#!/bin/bash


echo "### [start] ${0##*/} ${@} ###"

## env
echo "+++ (system-env) +++"
BASEDIR="$( cd $( dirname "$0" ) && pwd -P)"

## include
. ${BASEDIR}/include.sh


function curl_test() {
  echo "+++ (curl_test) curl_test +++"
  
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
    @(d|s)@(w|a)?(ww|dm)@(1|2)@(1|2))
      read -ra app_id_arr <<< $(GetSvrInfo "app_id" "app_id" "$1")
      ;;
  esac
  echo "app_id_arr=${app_id_arr[@]}"
  
  tot=${#app_id_arr[@]}
  idx=1
  for app_id in ${app_id_arr[@]}
  do
    read -r  port <<< $(GetSvrInfo "port" "app_id" "${app_id}")
    echo "[${idx}/${tot}] app_id=${app_id} port=${port}"
    
    ## [actual-code]
    # login
    response=$(curl --location --silent --request POST 'http://localhost:'${port}'/public/cmm/user/idlogin' \
    --header 'debug: Y' \
    --header 'Content-Type: application/json' \
    --data-raw '{
      "header": {},
      "body": {
        "userId": "1000000001"
      }
    }')
    echo "response=${response}"$'\n'
    ## //[actual-code]
    
    idx=$(( $idx + 1 ))
  done
  
  echo "--- //(curl_test) curl_test ---"
}



case "$1" in
  all)
    ;;
  @(d|s)?(ev|ta))
    ;;
  @(w|a)?(ww|dm))
    ;;
  @(d|s)@(w|a)?(ww|dm)@(1|2)@(1|2))
    ;;
  *)
    echo "Usage: ${0##*/} [all|\${profile_sys}|\${app_name_c3}|\${app_id}]"
    curl_test "${PROFILE_SYS}";
    exit 0;
    ;;
esac


curl_test "$1";

echo "### [finish] ${0##*/} ${@} ###"$'\n'$'\n'
