#!/bin/bash

echo "### [start] ${0##*/} ${@} ###"

## env
echo "+++ (system-env) +++"
SCRIPT_DIR="$( cd $( dirname "$0" ) && pwd -P)"

printf '%s\n' $(cat << EOF
SCRIPT_DIR=${SCRIPT_DIR}
EOF
)




## (transfer) transfer *.sh files
function transfer() {
  echo "+++ (transfer) transfer *.sh files +++"
  FILES=(
    "${BASEDIR}/script/*.sh"
  )
  echo "FILES=${FILES[*]}"
  
  for SVR in ${SVR_LIST[*]}
  do
    scp ${FILES[*]} ${EXEC_USER}@${SVR}:${APP_HOME}
    ssh ${EXEC_USER}@${SVR} "chmod u+x ${APP_HOME}/*.sh;"
  done
  echo "--- //(transfer) transfer *.sh files ---"
}


echo "+++ (runtime-env) +++"
EXEC_USER="tomcat"
PROFILE_SYS=$1
case "${PROFILE_SYS}" in
  dev)
    SVR_LIST=('172.28.200.30')
    APP_HOME="/app/pilot-dev"
    ;;
  sta)
    SVR_LIST=('172.28.200.30')
    APP_HOME="/app/pilot-sta"
    ;;
  *)
    exit -1
    ;;
esac
BASEDIR="$( cd ${SCRIPT_DIR} && pwd -P)"


printf '%s\n' $(cat << EOF
EXEC_USER=${EXEC_USER}
PROFILE_SYS=${PROFILE_SYS}
SVR_LIST=${SVR_LIST[*]}
APP_HOME=${APP_HOME}
BASEDIR=${BASEDIR}
EOF
)


transfer "${PROFILE_SYS}";

echo "### [finish] ${0##*/} ${@} ###"$'\n'$'\n'