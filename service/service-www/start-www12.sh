#!/bin/bash

APP_HOME=/app/WAS/pilot
JAR_NAME=service-www
APP_NAME=www12
SERVER_PORT=7101

JAVA_HOME=/prod/java/openjdk-11.0.2
PATH=$JAVA_HOME/bin:$PATH

PROFILE_SYS=$1

case ${PROFILE_SYS} in
  dev)
    ;;
  staging)
    ;;
  prod)
    ;;
  *)
    echo "set profile_sys=loc"
    PROFILE_SYS=loc
    ;;
esac
echo "PROFILE_SYS=${PROFILE_SYS}"

JAVA_OPTS="${JAVA_OPTS} -Dspring.profiles.active=${PROFILE_SYS}"
JAVA_OPTS="${JAVA_OPTS} -Dapp.name=${APP_NAME}"
JAVA_OPTS="${JAVA_OPTS} -Dserver.port=${SERVER_PORT}"

PS_CMD="ps -ef | grep -v grep | grep -v tail |  grep -v .sh | grep ${APP_NAME} | awk '{ print \$2 }'"
echo "${PS_CMD}"
_PID=$(eval "${PS_CMD}")

if [ "${_PID}" != "" ]; then
  echo "execute ${APP_HOME}/stop-www12.sh"
  ${APP_HOME}/stop-www12.sh
fi

FIND_CMD="find ${APP_HOME} -maxdepth 1 -type f -name ${JAR_NAME}*.jar | sort -V | tail -n 1"
echo "${FIND_CMD}"
JAR_FILE=$(eval "${FIND_CMD}")
echo "JAR_FILE=${JAR_FILE}"

JAVA_CMD="nohup $JAVA_HOME/bin/java ${JAVA_OPTS} -jar ${JAR_FILE} > /dev/null 2>&1 &"
echo "${JAVA_CMD}"
eval "${JAVA_CMD}"

echo "${PS_CMD}"
_PID=$(eval "${PS_CMD}")

if [ "${_PID}" != "" ]; then
  echo "${APP_NAME}(pid:'${_PID}') is started"
fi
