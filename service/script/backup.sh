#!/bin/bash

echo "### [start] ${0##*/} ${@} ###"

## env
echo $"+++ (system-env) +++"
BASEDIR="$( cd $( dirname "$0" ) && pwd -P)"

printf '%s\n' $(cat << EOF
BASEDIR=${BASEDIR}
EOF
)


## (backup) backup except for lastest jar
function backup() {
  echo "+++ (backup) backup except for lastest jar +++"
  if [ ! -e ${ARCHIVE_DIR} ]; then
    local mkdir_cmd="mkdir -p ${ARCHIVE_DIR}"
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
  
  local latest_cmd="ls -rt ${BASEDIR}/${APP_NAME}*.jar | tail -n 1"
  echo "latest_cmd=${latest_cmd}"
  latest_jar=$(eval "${latest_cmd}")
  
  old_cmd="find ${BASEDIR} -maxdepth 1 -type f ! -newer ${latest_jar} -name '${APP_NAME}*.jar' ! -samefile ${latest_jar}"
  echo "old_cmd=${old_cmd}"
  old_jar=$(eval "${old_cmd}")
  
  echo "latest_jar=${latest_jar}"
  echo "old_jar=${old_jar[*]}"
  
  if [ "${old_jar[*]}" != "" ]; then
    mv_cmd="mv $(echo ${old_jar[*]} | tr -d '\n') ${ARCHIVE_DIR}"
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
  echo "--- //(backup) backup except for lastest jar ---"
}


echo "+++ (runtime-env) +++"
EXEC_USER="tomcat"
ARCHIVE_DIR="${BASEDIR}/archive"
APP_NAME=$1
case "${APP_NAME}" in
  w*)
    APP_NAME="pilot-www"
    ;;
  a*)
    APP_NAME="pilot-adm"
    ;;
  *)
    echo "Usage: ${0##*/} [w|a]"
    exit -1
    ;;
esac


printf '%s\n' $(cat << EOF
EXEC_USER=${EXEC_USER}
ARCHIVE_DIR=${ARCHIVE_DIR}
EOF
)


backup;

echo "### [finish] ${0##*/} ${@} ###"$'\n'$'\n'