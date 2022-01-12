#!/bin/bash

## env
echo $'\n'"+++ (system-env) +++"
SCRIPT_DIR="$( cd $( dirname "$0" ) && pwd -P)"
BASEDIR="${SCRIPT_DIR}"

printf '%s\n' $(cat << EOF
SCRIPT_DIR=${SCRIPT_DIR}
BASEDIR=${BASEDIR}
EOF
)
echo "--- (system-env) ---"



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


echo "+++ (runtime-env) +++"
PROFILE_SYS=$1
if [ "$1" == "" ]; then
  PROFILE_SYS="dev"
fi
case ${PROFILE_SYS} in
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


printf '%s\n' $(cat << EOF
PROFILE_SYS=${PROFILE_SYS}
SVR_LIST=${SVR_LIST[*]}
APP_HOME=${APP_HOME}
EOF
)
echo "--- (runtime-env) ---"


transfer "${PROFILE_SYS}";

