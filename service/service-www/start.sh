#!/bin/bash

APP_HOME=/app/WAS/pilot
INST_ID=service-www

JAVA_HOME=/prod/java/openjdk-11.0.2
PATH=$JAVA_HOME/bin:$PATH

PROFILE_SYS=$1

case "$PROFILE_SYS" in
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

JAVA_OPTS="${JAVA_OPTS} -Dspring.profiles.active=${PROFILE_SYS}"

_PID=`ps -ef | grep -v grep | egrep ${INST_ID}.*\.jar | awk '{ print $2 }'`

if [ "${_PID}" != "" ]; then
  echo "execute ${APP_HOME}/stop.sh"
  ${APP_HOME}/stop.sh
fi

JAR_FILE=`find ${APP_HOME} -maxdepth 1 -type f -name ${INST_ID}*.jar | sort -V | tail -n 1`

nohup $JAVA_HOME/bin/java ${JAVA_OPTS} -jar ${JAR_FILE} > /dev/null 2>&1 &
