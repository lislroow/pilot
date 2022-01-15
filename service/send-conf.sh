#!/bin/bash

echo "### [start] ${0##*/} ${@} ###"

## env
echo "+++ (system-env) +++"
SCRIPT_DIR="$( cd $( dirname "$0" ) && pwd -P)"

printf '%s\n' $(cat << EOF
SCRIPT_DIR=${SCRIPT_DIR}
EOF
)



## (send_conf) transfer logback.xml, application.yaml
function send_conf() {
  echo "+++ (send_conf) transfer logback.xml, application.yaml ] +++"
  
  if [ "${PROFILE_SYS}" == "loc" ]; then
    for APP_HOME in ${APP_HOME_LIST[*]}
    do
      FILES=(
        "${BASEDIR}/${APP_NAME}/src/main/resources/.logback-${APP_NAME##*-}-${APP_HOME##*-}.xml"
      )
      echo "FILES=${FILES[*]}"
      if [ ! -e ${APP_HOME} ]; then
        MKDIR_CMD="mkdir -p ${APP_HOME}"
        echo "${MKDIR_CMD}"
        eval "${MKDIR_CMD}"
      fi
      CP_CMD="cp ${FILES[*]} ${APP_HOME}"
      echo "${CP_CMD}"
      eval "${CP_CMD}"
    done
  else
    for SVR in ${SVR_LIST[*]}
    do
      for APP_HOME in ${APP_HOME_LIST[*]}
      do
        FILES=(
          "${BASEDIR}/${APP_NAME}/src/main/resources/.logback-${APP_NAME##*-}-${APP_HOME##*-}.xml"
        )
        echo "FILES=${FILES[*]}"
        scp ${FILES[*]} ${EXEC_USER}@${SVR}:${APP_HOME}
      done
    done
  fi
  
  echo "--- //(send_conf) transfer logback.xml, application.yaml ---"
}


echo "+++ (runtime-env) +++"
BASEDIR="$( cd ${SCRIPT_DIR} && pwd -P)"
EXEC_USER="tomcat"
PROFILE_SYS=$1
case "${PROFILE_SYS}" in
  loc)
    APP_HOME_LIST=("/z/app/pilot-dev" "/z/app/pilot-sta")
    ;;
  dev)
    SVR_LIST=("172.28.200.30")
    APP_HOME_LIST=("/app/pilot-dev")
    ;;
  sta)
    SVR_LIST=("172.28.200.30")
    APP_HOME_LIST=("/app/pilot-sta")
    ;;
  -h)
    echo "Usage: ${0##*/} [dev|sta] [w|a]"
    exit 0;
    ;;
  *)
    echo "Usage: ${0##*/} [dev|sta] [w|a]"
    exit -1
    ;;
esac
APP_NAME=$2
case "${APP_NAME}" in
  *w*)
    APP_NAME="service-www"
    ;;
  *a*)
    APP_NAME="service-adm"
    ;;
  *)
    exit -1;
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


send_conf;

echo "### [finish] ${0##*/} ${@} ###"$'\n'$'\n'
