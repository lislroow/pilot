#!/bin/bash

APP_ID=$1

case ${APP_ID} in
  dwww11)
    ;;
  dwww12)
    ;;
  *)
    exit -1
    ;;
esac

PS_CMD="ps -ef | grep -v grep | grep -v tail |  grep -v .sh | grep ${APP_ID} | awk '{ print \$2 }'"
echo "${PS_CMD}"
_PID=$(eval "${PS_CMD}")

if [ "${_PID}" != "" ]; then
  echo "stopping ${APP_ID}(pid:'${_PID}')"
  kill -15 ${_PID}
  
  i=1
  while [ $i -lt 600 ];
  do
    _CHECK_PID=$(eval "${PS_CMD}")
    if [ "${_CHECK_PID}" == "" ]; then
      echo "${APP_ID}(pid:'${_PID}') killed"
      break
    fi
    echo "wait for ${APP_ID}(pid:'${_PID}') killing"
    i=$(( $i + 1 ))
    sleep 1
  done
else
  echo "${APP_ID} is not started"
fi
