#!/bin/bash

APP_HOME=/app/WAS/pilot
JAR_NAME=service-www
APP_NAME=www11
SERVER_PORT=7100
PROFILE_SYS=dev

JAVA_HOME=/prod/java/openjdk-11.0.2
PATH=$JAVA_HOME/bin:$PATH

if [ "$1" != "" ]; then
  JAR_FILE=$1
else
  FIND_CMD="ls -rt  ${APP_HOME}/${JAR_NAME}*.jar | sort -V | tail -n 1"
  echo "${FIND_CMD}"
  JAR_FILE=$(eval "${FIND_CMD}")
fi

printf '%s\n' $(cat << EOF
APP_HOME=${APP_HOME}
JAR_NAME=${JAR_NAME}
APP_NAME=${APP_NAME}
SERVER_PORT=${SERVER_PORT}
PROFILE_SYS=${PROFILE_SYS}
JAVA_HOME=${JAVA_HOME}
JAR_FILE=${JAR_FILE}
EOF
)

JAVA_OPTS="${JAVA_OPTS} -Dspring.profiles.active=${PROFILE_SYS}"
JAVA_OPTS="${JAVA_OPTS} -Dapp.name=${APP_NAME}"
JAVA_OPTS="${JAVA_OPTS} -Dserver.port=${SERVER_PORT}"

PS_CMD="ps -ef | grep -v grep | grep -v tail |  grep -v .sh | grep ${APP_NAME} | awk '{ print \$2 }'"
echo "${PS_CMD}"
_PID=$(eval "${PS_CMD}")

if [ "${_PID}" != "" ]; then
  echo "execute ${APP_HOME}/stop-www11.sh"
  ${APP_HOME}/stop-www11.sh
fi

JAVA_CMD="nohup $JAVA_HOME/bin/java ${JAVA_OPTS} -jar ${JAR_FILE} > /dev/null 2>&1 &"
echo "${JAVA_CMD}"
eval "${JAVA_CMD}"

echo "${PS_CMD}"
_PID=$(eval "${PS_CMD}")

if [ "${_PID}" != "" ]; then
  echo "${APP_NAME}(pid:'${_PID}') is started"
fi
