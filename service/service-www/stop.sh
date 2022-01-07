#!/bin/bash

APP_HOME=/app/WAS/proto
INST_ID=service-www

_PID=`ps -ef | grep -v grep | egrep ${INST_ID}.*\.jar | awk '{ print $2 }'`

if [ "${_PID}" != "" ]; then
  echo "stopping ${INST_ID}(pid:'${_PID}')"
  kill -15 $_PID
  
  i=1
  while [ $i -lt 600 ];
  do
    _CHECK_PID=`ps -ef | grep -v grep | egrep ${INST_ID}.*\.jar | awk '{ print $2 }'`
    if [ "${_CHECK_PID}" == "" ]; then
      echo "${INST_ID}(pid:'${_PID}') killed"
      break
    fi
    echo "wait for ${INST_ID}(pid:'${_PID}') killing"
    i=$(($i+1))
    sleep 1
  done
else
  echo "${INST_ID} is not started"
fi

