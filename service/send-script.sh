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
  echo "files=${files[*]}"
  
  for svr in ${SVR_LIST[*]}
  do
    for app_home in ${APP_HOME_LIST[*]}
    do
      scp ${files[*]} ${EXEC_USER}@${svr}:${app_home}
      ssh ${EXEC_USER}@${svr} "chmod u+x ${app_home}/*.sh;"
    done
  done
  echo "--- //(send_script) transfer *.sh files ---"
}


echo "+++ (runtime-env) +++"
EXEC_USER="tomcat"
PROFILE_SYS=$1
case "${PROFILE_SYS}" in
  dev)
    SVR_LIST=("172.28.200.30")
    APP_HOME_LIST=("/app/pilot-dev")
    ;;
  sta)
    SVR_LIST=("172.28.200.30")
    APP_HOME_LIST=("/app/pilot-sta")
    ;;
  -h)
    echo "Usage: ${0##*/} [dev|sta]"
    exit 0;
    ;;
  *)
    echo "Usage: ${0##*/} [dev|sta]"
    exit -1
    ;;
esac


printf '%s\n' $(cat << EOF
EXEC_USER=${EXEC_USER}
PROFILE_SYS=${PROFILE_SYS}
SVR_LIST=${SVR_LIST[*]}
APP_HOME_LIST=${APP_HOME_LIST[*]}
EOF
)


send_script;

echo "### [finish] ${0##*/} ${@} ###"$'\n'$'\n'
