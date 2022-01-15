#!/bin/bash

echo "### [start] ${0##*/} ${@} ###"

## env
echo "+++ (system-env) +++"
SCRIPT_DIR="$( cd $( dirname "$0" ) && pwd -P)"

UNAME=`uname -s`
if [[ "${UNAME}" = "Linux"* ]]; then
  OS_NAME="linux"
elif [[ "${UNAME}" = "CYGWIN"* || "${UNAME}" = "MINGW"* ]]; then
  OS_NAME="win"
fi

case "${OS_NAME}" in
  linux)
    JAVA_HOME=/prod/java/openjdk-11.0.13.8-temurin
    PATH=$JAVA_HOME/bin:$PATH
    ;;
  win)
    JAVA_HOME=/z/develop/java/openjdk-11.0.13.8-temurin
    PATH=$JAVA_HOME/bin:$PATH
    ;;
  *)
    echo "invalid os"
    exit -1
    ;;
esac

printf '%s\n' $(cat << EOF
SCRIPT_DIR=${SCRIPT_DIR}
UNAME=${UNAME}
OS_NAME=${OS_NAME}
JAVA_HOME=${JAVA_HOME}
EOF
)


## (start) start
function start() {
  echo "+++ (start) start +++"
  PS_CMD="ps -ef | grep -v grep | grep -v tail |  grep -v .sh | grep ${APP_ID} | awk '{ print \$2 }'"
  echo "${PS_CMD}"
  _PID=$(eval "${PS_CMD}")
  
  if [ "${_PID}" != "" ]; then
    echo "execute ${BASEDIR}/stop.sh ${APP_ID}"
    ${BASEDIR}/stop.sh ${APP_ID}
  fi
  
  # console-logfile
  LOG_FILEPATH="${LOGBASE}/${APP_ID}-console.log"
  if [ -e ${LOG_FILEPATH} ]; then
    curr_ts=`date +'%Y%m%d_%H%M%S'`
    LOG_BACKUP="${LOGBASE}/backup/${APP_ID}-console-${curr_ts}.log"
    if [ ! -e "${LOGBASE}/backup" ]; then
      MKDIR_CMD="mkdir -p ${LOGBASE}/backup"
      echo "${MKDIR_CMD}"
      if [ $(whoami) == "root" ]; then
        su ${EXEC_USER} -c "${MKDIR_CMD}"
      elif [ $(whoami) == ${EXEC_USER} ]; then
        eval "${MKDIR_CMD}"
      else
        echo "current user "$(whoami)
        exit -1
      fi
    fi
    MV_CMD="mv ${LOG_FILEPATH} ${LOG_BACKUP}"
    echo "${MV_CMD}"
    if [ $(whoami) == "root" ]; then
      su ${EXEC_USER} -c "${MV_CMD}"
    elif [ $(whoami) == ${EXEC_USER} ]; then
      eval "${MV_CMD}"
    else
      echo "current user "$(whoami)
      exit -1
    fi
  fi
  
  JAVA_OPTS=""
  JAVA_OPTS="${JAVA_OPTS} -Dspring.profiles.active=${PROFILE_SYS}"
  JAVA_OPTS="${JAVA_OPTS} -Dspring.output.ansi.enabled=always"
  JAVA_OPTS="${JAVA_OPTS} -Dapp.name=${APP_NAME}"
  JAVA_OPTS="${JAVA_OPTS} -Dapp.id=${APP_ID}"
  JAVA_OPTS="${JAVA_OPTS} -Dserver.port=${SERVER_PORT}"
  
  JAVA_CMD="nohup $JAVA_HOME/bin/java ${JAVA_OPTS} -jar ${JAR_FILE} > ${LOG_FILEPATH} 2>&1 &"
  echo "${JAVA_CMD}"
  if [ $(whoami) == "root" ]; then
    su ${EXEC_USER} -c "${JAVA_CMD}"
  elif [ $(whoami) == ${EXEC_USER} ]; then
    eval "${JAVA_CMD}"
  else
    echo "current user "$(whoami)
    exit -1
  fi
  
  echo "${PS_CMD}"
  _PID=$(eval "${PS_CMD}")
  
  if [ "${_PID}" == "" ]; then
    echo "${APP_ID} is not started"
    exit -1
  else
    echo "${APP_ID}(pid:'${_PID}') starting ..."
  fi
  
  i=1
  while [ $i -lt 600 ];
  do
    HTTP_CODE=$(curl --write-out "%{http_code}" --silent --output /dev/null "http://localhost:${SERVER_PORT}/")
    if [ "${HTTP_CODE}" == "200" ]; then
      echo "${APP_ID}(pid:'${_PID}') started"
      break
    fi
    echo "${APP_ID}(pid:'${_PID}') booting ..."
    i=$(( $i + 1 ))
    sleep 3
  done
  echo "--- //(start) start ---"
}


echo "+++ (runtime-env) +++"
EXEC_USER="tomcat"
BASEDIR="$( cd ${SCRIPT_DIR} && pwd -P)"
LOGBASE="/outlog/pilot"
APP_ID=$1
case "${APP_ID}" in
  dw*1)
    APP_NAME="service-www"
    APP_ID="dwww11"
    SERVER_PORT="7100"
    PROFILE_SYS="dev"
    ;;
  dw*2)
    APP_NAME="service-www"
    APP_ID="dwww12"
    SERVER_PORT="7101"
    PROFILE_SYS="dev"
    ;;
  sw*1)
    APP_NAME="service-www"
    APP_ID="swww11"
    SERVER_PORT="9100"
    PROFILE_SYS="sta"
    ;;
  sw*2)
    APP_NAME="service-www"
    APP_ID="swww12"
    SERVER_PORT="9101"
    PROFILE_SYS="sta"
    ;;
  da*1)
    APP_NAME="service-adm"
    APP_ID="dadm11"
    SERVER_PORT="7200"
    PROFILE_SYS="dev"
    ;;
  da*2)
    APP_NAME="service-adm"
    APP_ID="dadm12"
    SERVER_PORT="7201"
    PROFILE_SYS="dev"
    ;;
  sa*1)
    APP_NAME="service-adm"
    APP_ID="sadm11"
    SERVER_PORT="9200"
    PROFILE_SYS="sta"
    ;;
  sa*2)
    APP_NAME="service-adm"
    APP_ID="sadm12"
    SERVER_PORT="9201"
    PROFILE_SYS="sta"
    ;;
  *)
    exit -1
    ;;
esac

if [ "$2" != "" ]; then
  JAR_FILE=${BASEDIR}/$2
  if [ ! -e ${JAR_FILE} ]; then
    echo "JAR_FILE(${JAR_FILE}) does not exist"
    exit -1
  fi
else
  find_cmd="ls -rt ${BASEDIR}/${APP_NAME}*.jar | sort -V | tail -n 1"
  echo "${find_cmd}"
  JAR_FILE=$(eval "${find_cmd}")
fi


printf '%s\n' $(cat << EOF
EXEC_USER=${EXEC_USER}
BASEDIR=${BASEDIR}
APP_NAME=${APP_NAME}
APP_ID=${APP_ID}
SERVER_PORT=${SERVER_PORT}
PROFILE_SYS=${PROFILE_SYS}
JAR_FILE=${JAR_FILE}
EOF
)


start;

echo "### [finish] ${0##*/} ${@} ###"$'\n'$'\n'
