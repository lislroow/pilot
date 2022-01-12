#!/bin/bash

## env
echo "+++ (env) +++"
SCRIPT_DIR="$( cd $( dirname "$0" ) && pwd -P)"
BASEDIR="${SCRIPT_DIR}"

printf '%s\n' $(cat << EOF
SCRIPT_DIR=${SCRIPT_DIR}
EOF
)
echo "--- (env) ---"

## (transfer) transfer *.sh files
function transfer() {
  echo "+++ (transfer) transfer *.sh files +++"
  
  FILES=(
    "${BASEDIR}/*.sh"
  )
  echo "FILES=${FILES[*]}"
  
  for SVR in ${SVR_LIST[*]}
  do
    scp ${FILES[*]} root@${SVR}:${APP_HOME}
    ssh root@${SVR} "chmod u+x ${APP_HOME}/*.sh;"
  done
  
  echo "--- (transfer) transfer *.sh files ---"$'\n'
}

PROFILE_SYS=$1

if [ "$1" == "" ]; then
  PROFILE_SYS="dev"
fi

case $PROFILE_SYS in
  dev)
    SVR_LIST=('172.28.200.30')
    APP_HOME="/app/WAS/pilot"
    ;;
  sta*)
    exit -1
    ;;
  *)
    exit -1
    ;;
esac
echo "SVR_LIST=${SVR_LIST[*]}"
echo "APP_HOME=${APP_HOME}"

transfer "${PROFILE_SYS}";
