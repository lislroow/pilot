#!/bin/bash

## env
UNAME=`uname -s`
#echo "UNAME=${UNAME}"
if [[ "${UNAME}" = "Linux"* ]]; then
  OS_NAME="linux"
elif [[ "${UNAME}" = "CYGWIN"* || "${UNAME}" = "MINGW"* ]]; then
  OS_NAME="win"
fi

#echo "OS_NAME=${OS_NAME}"

case ${OS_NAME} in
  linux)
    M2_HOME=/prod/maven/maven
    JAVA_HOME=/prod/java/openjdk-11.0.13.8-temurin
    PATH=$JAVA_HOME/bin:$M2_HOME/bin:$PATH
    ;;
  win)
    M2_HOME=/z/develop/build/maven-3.6.3
    JAVA_HOME=/z/develop/java/openjdk-11.0.13.8-temurin
    PATH=$JAVA_HOME/bin:$M2_HOME/bin:$PATH
    ;;
  *)
    echo "invalid os"
    exit -1
    ;;
esac

APP_HOME=/app/WAS/pilot
APP_NAME=service-www

APP_ID=$1

case ${APP_ID} in
  dwww11)
    SERVER_PORT=7100
    PROFILE_SYS=dev
    ;;
  dwww12)
    SERVER_PORT=7101
    PROFILE_SYS=dev
    ;;
  *)
    exit -1
    ;;
esac

if [ "$2" != "" ]; then
  JAR_FILE=${APP_HOME}/$2
  if [ ! -e ${JAR_FILE} ]; then
    echo "JAR_FILE(${JAR_FILE}) does not exist"
    exit -1
  fi
else
  FIND_CMD="ls -rt  ${APP_HOME}/${APP_NAME}*.jar | sort -V | tail -n 1"
  echo "${FIND_CMD}"
  JAR_FILE=$(eval "${FIND_CMD}")
fi

printf '%s\n' $(cat << EOF
APP_HOME=${APP_HOME}
APP_NAME=${APP_NAME}
APP_ID=${APP_ID}
SERVER_PORT=${SERVER_PORT}
PROFILE_SYS=${PROFILE_SYS}
JAVA_HOME=${JAVA_HOME}
JAR_FILE=${JAR_FILE}
EOF
)

PS_CMD="ps -ef | grep -v grep | grep -v tail |  grep -v .sh | grep ${APP_ID} | awk '{ print \$2 }'"
echo "${PS_CMD}"
_PID=$(eval "${PS_CMD}")

if [ "${_PID}" != "" ]; then
  echo "execute ${APP_HOME}/stop.sh ${APP_ID}"
  ${APP_HOME}/stop.sh ${APP_ID}
fi

JAVA_OPTS=""
JAVA_OPTS="${JAVA_OPTS} -Dspring.profiles.active=${PROFILE_SYS}"
JAVA_OPTS="${JAVA_OPTS} -Dapp.id=${APP_ID}"
JAVA_OPTS="${JAVA_OPTS} -Dserver.port=${SERVER_PORT}"

JAVA_CMD="nohup $JAVA_HOME/bin/java ${JAVA_OPTS} -jar ${JAR_FILE} > /dev/null 2>&1 &"
echo "${JAVA_CMD}"
eval "${JAVA_CMD}"

echo "${PS_CMD}"
_PID=$(eval "${PS_CMD}")

if [ "${_PID}" != "" ]; then
  echo "${APP_ID}(pid:'${_PID}') is starting"
fi

i=1
while [ $i -lt 600 ];
do
  HTTP_CODE=$(curl --write-out "%{http_code}" --silent --output /dev/null "http://localhost:${SERVER_PORT}/")
  if [ "${HTTP_CODE}" == "200" ]; then
    echo "${APP_ID}(pid:'${_PID}') is started"
    break
  fi
  echo "${APP_ID}(pid:'${_PID}') is booting"
  i=$(( $i + 1 ))
  sleep 1
done

