#!/bin/bash

echo "### [start] ${0##*/} ${@} ###"

## env
echo "+++ (system-env) +++"
BASEDIR="$( cd $( dirname "$0" ) && pwd -P)"

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
BASEDIR=${BASEDIR}
UNAME=${UNAME}
OS_NAME=${OS_NAME}
JAVA_HOME=${JAVA_HOME}
EOF
)


## (start) start
function start() {
  echo "+++ (start) start +++"
  local ps_cmd="ps -ef | grep -v grep | grep -v tail |  grep -v .sh | grep ${APP_ID} | awk '{ print \$2 }'"
  echo "ps_cmd=${ps_cmd}"
  local _pid=$(eval "${ps_cmd}")
  
  if [ "${_pid}" != "" ]; then
    echo "execute ${BASEDIR}/stop.sh ${APP_ID}"
    ${BASEDIR}/stop.sh ${APP_ID}
  fi
  
  # console-logfile
  local log_filepath="${LOGBASE}/${APP_ID}-console.log"
  echo "log_filepath=${log_filepath}"
  if [ -e ${log_filepath} ]; then
    local curr_ts=`date +'%Y%m%d_%H%M%S'`
    local bak_filepath="${LOGBASE}/backup/${APP_ID}-console-${curr_ts}.log"
    echo "bak_filepath=${bak_filepath}"
    if [ ! -e "${LOGBASE}/backup" ]; then
      local mkdir_cmd="mkdir -p ${LOGBASE}/backup"
      echo "mkdir_cmd=${mkdir_cmd}"
      if [ $(whoami) == "root" ]; then
        su ${EXEC_USER} -c "${mkdir_cmd}"
      elif [ $(whoami) == ${EXEC_USER} ]; then
        eval "${mkdir_cmd}"
      else
        echo "current user "$(whoami)
        exit -1
      fi
    fi
    local mv_cmd="mv ${log_filepath} ${bak_filepath}"
    echo "mv_cmd=${mv_cmd}"
    if [ $(whoami) == "root" ]; then
      su ${EXEC_USER} -c "${mv_cmd}"
    elif [ $(whoami) == ${EXEC_USER} ]; then
      eval "${mv_cmd}"
    else
      echo "current user "$(whoami)
      exit -1
    fi
  fi
  
  local java_opts=""
  java_opts="${java_opts} -Dspring.profiles.active=${PROFILE_SYS}"
  java_opts="${java_opts} -Dspring.output.ansi.enabled=always"
  java_opts="${java_opts} -Dapp.name=${APP_NAME}"
  java_opts="${java_opts} -Dapp.id=${APP_ID}"
  java_opts="${java_opts} -Dserver.port=${SERVER_PORT}"
  
  local java_cmd="nohup $JAVA_HOME/bin/java ${java_opts} -jar ${JAR_FILE} > ${log_filepath} 2>&1 &"
  echo "java_cmd=${java_cmd}"
  if [ $(whoami) == "root" ]; then
    su ${EXEC_USER} -c "${java_cmd}"
  elif [ $(whoami) == ${EXEC_USER} ]; then
    eval "${java_cmd}"
  else
    echo "current user "$(whoami)
    exit -1
  fi
  
  echo "ps_cmd=${ps_cmd}"
  local _pid=$(eval "${ps_cmd}")
  echo "_pid=${_pid}"
  
  if [ "${_pid}" == "" ]; then
    echo "${APP_ID} is not started"
    exit -1
  else
    echo "${APP_ID}(pid:'${_pid}') starting ..."
  fi
  
  i=1
  while [ $i -lt 600 ];
  do
    local http_code=$(curl --write-out "%{http_code}" --silent --output /dev/null "http://localhost:${SERVER_PORT}/")
    if [ "${http_code}" == "200" ]; then
      echo "${APP_ID}(pid:'${_pid}') started"
      break
    fi
    echo "${APP_ID}(pid:'${_pid}') booting ..."
    i=$(( $i + 1 ))
    sleep 3
  done
  echo "--- //(start) start ---"
}


echo "+++ (runtime-env) +++"
EXEC_USER="tomcat"
LOGBASE="/outlog/pilot"
APP_ID=$1
case "${APP_ID}" in
  dw*1)
    APP_NAME="pilot-www"
    APP_ID="dwww11"
    SERVER_PORT="7100"
    PROFILE_SYS="dev"
    ;;
  dw*2)
    APP_NAME="pilot-www"
    APP_ID="dwww12"
    SERVER_PORT="7101"
    PROFILE_SYS="dev"
    ;;
  sw*1)
    APP_NAME="pilot-www"
    APP_ID="swww11"
    SERVER_PORT="9100"
    PROFILE_SYS="sta"
    ;;
  sw*2)
    APP_NAME="pilot-www"
    APP_ID="swww12"
    SERVER_PORT="9101"
    PROFILE_SYS="sta"
    ;;
  da*1)
    APP_NAME="pilot-adm"
    APP_ID="dadm11"
    SERVER_PORT="7200"
    PROFILE_SYS="dev"
    ;;
  da*2)
    APP_NAME="pilot-adm"
    APP_ID="dadm12"
    SERVER_PORT="7201"
    PROFILE_SYS="dev"
    ;;
  sa*1)
    APP_NAME="pilot-adm"
    APP_ID="sadm11"
    SERVER_PORT="9200"
    PROFILE_SYS="sta"
    ;;
  sa*2)
    APP_NAME="pilot-adm"
    APP_ID="sadm12"
    SERVER_PORT="9201"
    PROFILE_SYS="sta"
    ;;
  *)
    echo "Usage: ${0##*/} [dw1|dw2|da1|da2|sw1|sw2|sa1|sa2]"
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


start;

echo "### [finish] ${0##*/} ${@} ###"$'\n'$'\n'
