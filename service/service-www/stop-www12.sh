#!/bin/bash

APP_HOME=/app/WAS/pilot
JAR_NAME=service-www
APP_NAME=www11

PS_CMD="ps -ef | grep -v grep | grep Dapp.name=${APP_NAME} | awk '{ print \$2 }'"
echo "${PS_CMD}"
_PID=$(eval "${PS_CMD}")

if [ "${_PID}" != "" ]; then
  echo "stopping ${APP_NAME}(pid:'${_PID}')"
  kill -15 ${_PID}
  
  i=1
  while [ $i -lt 600 ];
  do
    _CHECK_PID=$(eval "${PS_CMD}")
    if [ "${_CHECK_PID}" == "" ]; then
      echo "${APP_NAME}(pid:'${_PID}') killed"
      break
    fi
    echo "wait for ${APP_NAME}(pid:'${_PID}') killing"
    i=$(( $i + 1 ))
    sleep 1
  done
else
  echo "${APP_NAME} is not started"
fi
