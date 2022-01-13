#!/bin/bash

echo "### [start] ${0##*/} ${@} ###"

## env
echo "+++ (system-env) +++"
SCRIPT_DIR="$( cd $( dirname "$0" ) && pwd -P)"
BASEDIR="${SCRIPT_DIR}"

UNAME=`uname -s`
if [[ "${UNAME}" = "Linux"* ]]; then
  OS_NAME="linux"
elif [[ "${UNAME}" = "CYGWIN"* || "${UNAME}" = "MINGW"* ]]; then
  OS_NAME="win"
fi

case ${OS_NAME} in
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
BASEDIR=${BASEDIR}
UNAME=${UNAME}
OS_NAME=${OS_NAME}
JAVA_HOME=${JAVA_HOME}
EOF
)
echo "--- (system-env) ---"



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
  
  JAVA_OPTS=""
  JAVA_OPTS="${JAVA_OPTS} -Dspring.profiles.active=${PROFILE_SYS}"
  JAVA_OPTS="${JAVA_OPTS} -Dapp.id=${APP_ID}"
  JAVA_OPTS="${JAVA_OPTS} -Dserver.port=${SERVER_PORT}"
  
  JAVA_CMD="nohup $JAVA_HOME/bin/java ${JAVA_OPTS} -jar ${JAR_FILE} > /dev/null 2>&1 &"
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
  
  if [ "${_PID}" != "" ]; then
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
  echo "--- (start) start ---"
}


echo "+++ (runtime-env) +++"
EXEC_USER="tomcat"
APP_NAME="service-www"
APP_ID=$1
case ${APP_ID} in
  dw*11)
    APP_ID=dwww11
    SERVER_PORT="7100"
    PROFILE_SYS="dev"
    ;;
  dw*12)
    APP_ID=dwww12
    SERVER_PORT="7101"
    PROFILE_SYS="dev"
    ;;
  sw*11)
    APP_ID=swww11
    SERVER_PORT="9100"
    PROFILE_SYS="staging"
    ;;
  sw*12)
    APP_ID=swww12
    SERVER_PORT="9101"
    PROFILE_SYS="staging"
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
APP_NAME=${APP_NAME}
APP_ID=${APP_ID}
SERVER_PORT=${SERVER_PORT}
PROFILE_SYS=${PROFILE_SYS}
JAR_FILE=${JAR_FILE}
EOF
)
echo "--- (runtime-env) ---"

start;

echo "### [finish] ${0##*/} ${@} ###"$'\n'
