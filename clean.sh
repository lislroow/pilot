#!/bin/bash

echo "### [start] ${0##*/} ${@} ###"

## env
echo "+++ (system-env) +++"
UNAME=`uname -s`

echo "UNAME=${UNAME}"
if [[ "${UNAME}" = "Linux"* ]]; then
  OS_NAME="linux"
elif [[ "${UNAME}" = "CYGWIN"* || "${UNAME}" = "MINGW"* ]]; then
  OS_NAME="win"
fi

echo "OS_NAME=${OS_NAME}"

case "${OS_NAME}" in
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

printf '%s\n' $(cat << EOF
M2_HOME=${M2_HOME}
JAVA_HOME=${JAVA_HOME}
EOF
)



## (clean) delete ${HOME}/.m2/repository/mgkim
function clean() {
  echo "+++ (clean) delete ${HOME}/.m2/repository/mgkim +++"
  
  ## 2022.01.14 주석처리
  #MVN_ARGS=""
  #MVN_ARGS="${MVN_ARGS} "
  #MVN_ARGS="${MVN_ARGS} -Dexpression=settings.localRepository"
  #MVN_ARGS="${MVN_ARGS} -DforceStdout"
  #MVN_ARGS="${MVN_ARGS} --quiet"
  #
  #MVN_CMD="mvn ${MVN_ARGS} help:evaluate"
  #echo "MVN_CMD=${MVN_CMD}"
  #LOC_M2=$(eval "${MVN_CMD}")
  
  ## 2022.01.14 대체 코드
  LOC_M2=${HOME}/.m2/repository
  echo "LOC_M2=${LOC_M2}"
  if [ -e ${LOC_M2} ]; then
    RM_CMD="rm -rf ${LOC_M2}/mgkim"
    echo "${RM_CMD}"
    eval "${RM_CMD}"
  fi
  
  LS_CMD="ls -al ${LOC_M2} | grep mgkim"
  echo "${LS_CMD}"
  eval "${LS_CMD}"
  
  echo "--- //(clean) delete ${HOME}/.m2/repository/mgkim ---"
}


clean;


echo "### [finish] ${0##*/} ${@} ###"$'\n'$'\n'
