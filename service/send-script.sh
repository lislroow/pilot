#!/bin/bash

echo "### [start] ${0##*/} ${@} ###"

## env
echo "+++ (system-env) +++"
SCRIPT_DIR="$( cd $( dirname "$0" ) && pwd -P)"

printf '%s\n' $(cat << EOF
SCRIPT_DIR=${SCRIPT_DIR}
EOF
)




## (send_script) transfer *.sh files
function send_script() {
  echo "+++ (send_script) transfer *.sh files +++"
  FILES=(
    "${BASEDIR}/script/*.sh"
  )
  echo "FILES=${FILES[*]}"
  
  for SVR in ${SVR_LIST[*]}
  do
    for APP_HOME in ${APP_HOME_LIST[*]}
    do
      scp ${FILES[*]} ${EXEC_USER}@${SVR}:${APP_HOME}
      ssh ${EXEC_USER}@${SVR} "chmod u+x ${APP_HOME}/*.sh;"
    done
  done
  echo "--- //(send_script) transfer *.sh files ---"
}


echo "+++ (runtime-env) +++"
BASEDIR="$( cd ${SCRIPT_DIR} && pwd -P)"
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
BASEDIR=${BASEDIR}
EXEC_USER=${EXEC_USER}
PROFILE_SYS=${PROFILE_SYS}
SVR_LIST=${SVR_LIST[*]}
APP_HOME=${APP_HOME}
EOF
)


send_script;

echo "### [finish] ${0##*/} ${@} ###"$'\n'$'\n'
