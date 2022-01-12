#!/bin/bash

## env
echo "+++ (system-env) +++"
UNAME=`uname -s`
if [[ "${UNAME}" = "Linux"* ]]; then
  OS_NAME="linux"
elif [[ "${UNAME}" = "CYGWIN"* || "${UNAME}" = "MINGW"* ]]; then
  OS_NAME="win"
fi

case ${OS_NAME} in
  linux)
    JAVA_HOME=/prod/java/openjdk-11.0.13.8-temurin
    M2_HOME=/prod/maven/maven
    PATH=$JAVA_HOME/bin:$M2_HOME/bin:$PATH
    ;;
  win)
    JAVA_HOME=/z/develop/java/openjdk-11.0.13.8-temurin
    M2_HOME=/z/develop/build/maven-3.6.3
    PATH=$JAVA_HOME/bin:$M2_HOME/bin:$PATH
    ;;
  *)
    echo "invalid os"
    exit -1
    ;;
esac

SCRIPT_DIR="$( cd $( dirname "$0" ) && pwd -P)"
BASEDIR="$( cd ${SCRIPT_DIR}/.. && pwd -P)"

printf '%s\n' $(cat << EOF
SCRIPT_DIR=${SCRIPT_DIR}
BASEDIR=${BASEDIR}
UNAME=${UNAME}
OS_NAME=${OS_NAME}
JAVA_HOME=${JAVA_HOME}
M2_HOME=${M2_HOME}
EOF
)
echo "--- (system-env) ---"


## (build) build maven project
function build() {
  echo "+++ (build) build maven project +++"
  
  MVN_ARGS=""
  MVN_ARGS="${MVN_ARGS} --file ${BASEDIR}/pom.xml"
  MVN_ARGS="${MVN_ARGS} -Dfile.encoding=utf-8"
  MVN_ARGS="${MVN_ARGS} -Dmaven.test.skip=true"
  MVN_ARGS="${MVN_ARGS} --update-snapshots"
  MVN_ARGS="${MVN_ARGS} --batch-mode"
  MVN_ARGS="${MVN_ARGS} --quiet"
  
  MVN_CMD="mvn ${MVN_ARGS} clean package spring-boot:repackage"
  echo "${MVN_CMD}"
  eval "${MVN_CMD}"
  
  JAR_FILE=$(xmlstarlet sel -N x="http://maven.apache.org/POM/4.0.0" \
    -t -v \
    "concat(x:project/x:artifactId, '-', x:project/x:version, '.', x:project/x:packaging)" \
    ${BASEDIR}/pom.xml)
  echo "JAR_FILE=${JAR_FILE}"
  
  # jar 파일명에 "-SNAPSHOT" 이 있으면 snapshot 저장소에 deploy 되어야 합니다. 
  if [[ "${JAR_FILE}" = *"-SNAPSHOT"* ]]; then
    NX_REPO=snapshot
  else
    NX_REPO=release
  fi
  
  ## deploy-nexus
  MVN_ARGS=""
  MVN_ARGS="${MVN_ARGS} -DpomFile=${BASEDIR}/pom.xml"
  MVN_ARGS="${MVN_ARGS} -Dfile=${BASEDIR}/target/${JAR_FILE}"
  MVN_ARGS="${MVN_ARGS} --quiet"
  MVN_ARGS="${MVN_ARGS} -DrepositoryId=maven-${NX_REPO}"
  MVN_ARGS="${MVN_ARGS} -Durl=https://nexus/repository/maven-${NX_REPO}/"
  
  MVN_CMD="mvn deploy:deploy-file ${MVN_ARGS}"
  echo "${MVN_CMD}"
  eval "${MVN_CMD}"
  
  echo "--- (build) build maven project ---"$'\n'
}


## (deploy) deploying trigger
function deploy() {
  echo "+++ (deploy) deploying trigger +++"
  for SVR in ${SVR_LIST[*]}
  do
    ssh root@${SVR} "${APP_HOME}/deploy.sh ${PROFILE_SYS}"
  done
  
  echo "--- (deploy) deploying trigger ---"$'\n'
}


echo "+++ (runtime-env) +++"
PROFILE_SYS=$1

if [ "$1" == "" ]; then
  PROFILE_SYS="dev"
fi

case ${PROFILE_SYS} in
  dev)
    SVR_LIST=('172.28.200.30')
    APP_HOME="/app/WAS/pilot"
    ;;
  sta*)
    exit -1
    ;;
  *)
    exit -1
    ;;
esac


printf '%s\n' $(cat << EOF
PROFILE_SYS=${PROFILE_SYS}
SVR_LIST=${SVR_LIST}
APP_HOME=${APP_HOME}
EOF
)
echo "--- (runtime-env) ---"

case $2 in
  build)
    build
    ;;
  deploy)
    deploy
    ;;
  *)
    build
    deploy
    ;;
esac