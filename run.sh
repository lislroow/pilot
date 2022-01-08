#!/bin/bash

UNAME=`uname -s`
echo "UNAME=${UNAME}"
if [[ "${UNAME}" = "Linux"* ]]; then
  OS_NAME="linux"
elif [[ "${UNAME}" = "CYGWIN"* || "${UNAME}" = "MINGW"* ]]; then
  OS_NAME="win"
fi

echo "OS_NAME=${OS_NAME}"

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

printf '%s\n' $(cat << EOF
M2_HOME=${M2_HOME}
JAVA_HOME=${JAVA_HOME}
EOF
)

function del() {
  LOC_M2=`mvn help:evaluate -Dexpression=settings.localRepository -q -DforceStdout`
  if [ -e ${LOC_M2} ]; then
    rm -rf ${LOC_M2}/mgkim
  fi
}

function install() {
  PROJECT_LIST=(
    'bom/framework-bom'
    'bom/service-bom'
    'framework/framework-batch' 
    'framework/framework-core'
    'framework/framework-daemon'
    'framework/framework-online'
  )
  echo "${PROJECT_LIST[*]}"
  
  idx=0
  for PROJECT in ${PROJECT_LIST[*]}
  do
    idx=$(( $idx + 1 ))
    
    PROJECT_BASE=./${PROJECT}
    
    ## build
    MVN_ARGS=""
    MVN_ARGS="${MVN_ARGS} --file ${PROJECT_BASE}/pom.xml"
    MVN_ARGS="${MVN_ARGS} -Dfile.encoding=utf-8"
    MVN_ARGS="${MVN_ARGS} -Dmaven.test.skip=true"
    MVN_ARGS="${MVN_ARGS} --update-snapshots"
    MVN_ARGS="${MVN_ARGS} --quiet"
    
    MVN_CMD="mvn ${MVN_ARGS} clean install"
    eval "${MVN_CMD}"
    echo "[${idx}/${#PROJECT_LIST[*]}] ${MVN_CMD}"
  done
}


case $1 in
  del)
    del
    ;;
  install)
  *)
    install
    ;;
esac
