#!/bin/bash

## env
. ./env.sh

PROJECT_BASE="$( cd $( ${DIRNAME_CMD} "$0" )/.. && pwd -P)"
echo "${PROJECT_BASE}"

PROFILE_SYS=dev

printf '%s\n' $(cat << EOF
UNAME=${UNAME}
OS_NAME=${OS_NAME}
M2_HOME=${M2_HOME}
JAVA_HOME=${JAVA_HOME}
APP_HOME=${APP_HOME}
APP_NAME=${JAVA_HOME}
APP_ID=${APP_ID}
SERVER_PORT=${SERVER_PORT}
PROFILE_SYS=${PROFILE_SYS}
PROJECT_BASE=${PROJECT_BASE}
EOF
)

## (build) build maven project
function build() {
  echo "+++ (build) build maven project +++"
  
  MVN_ARGS=""
  MVN_ARGS="${MVN_ARGS} --file ${PROJECT_BASE}/pom.xml"
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
    ${PROJECT_BASE}/pom.xml)
  
  echo "JAR_FILE=${JAR_FILE}"
  
  CHECKSUM=$(${MD5SUM_CMD} ${PROJECT_BASE}/target/${JAR_FILE} | awk '{ print substr($1, 1, 4) }')
  JAR_MD5_FILE=${JAR_FILE%.*}_${CHECKSUM}.${JAR_FILE##*.}
  cp ${PROJECT_BASE}/target/${JAR_FILE} ${PROJECT_BASE}/target/${JAR_MD5_FILE}
  
  echo "JAR_FILE=${JAR_FILE}"
  echo "JAR_MD5_FILE=${JAR_MD5_FILE}"
  
  JAR_FILE_PTRN=$(xmlstarlet sel -N x="http://maven.apache.org/POM/4.0.0" \
    -t -v \
    "concat(x:project/x:artifactId, '-*.', x:project/x:packaging)" \
    ${PROJECT_BASE}/pom.xml)
  
  echo "JAR_FILE_PTRN=${JAR_FILE_PTRN}"
  
  if [ ! -e ${PROJECT_BASE}/target/$JAR_MD5_FILE ]; then
    echo "build output file '${JAR_MD5_FILE}' is not found."
  fi
  
  # jar 파일명에 "-SNAPSHOT" 이 있으면 snapshot 저장소에 deploy 되어야 합니다. 
  if [[ "${JAR_MD5_FILE}" = *"-SNAPSHOT"* ]]; then
    NX_REPO=snapshot
  else
    NX_REPO=release
  fi
  
  ## deploy-nexus
  MVN_ARGS=""
  MVN_ARGS="${MVN_ARGS} -DpomFile=${PROJECT_BASE}/pom.xml"
  MVN_ARGS="${MVN_ARGS} -Dfile=${PROJECT_BASE}/target/${JAR_MD5_FILE}"
  MVN_ARGS="${MVN_ARGS} --quiet"
  MVN_ARGS="${MVN_ARGS} -DrepositoryId=maven-${NX_REPO}"
  MVN_ARGS="${MVN_ARGS} -Durl=https://nexus/repository/maven-${NX_REPO}/"
  
  MVN_CMD="mvn deploy:deploy-file ${MVN_ARGS}"
  echo "${MVN_CMD}"
  eval "${MVN_CMD}"
  
  echo "--- (build) build maven project ---"$'\n'
}

## (transfer) transfer *.sh files
function transfer() {
  echo "+++ (transfer) transfer *.sh files +++"
  
  case $PROFILE_SYS in
    dev)
      SVR_LIST=(
        '172.28.200.30'
      )
      APP_HOME=/app/WAS/pilot
    ;;
    sta*)
    ;;
  esac
  echo "SVR_LIST=${SVR_LIST[*]}"
  echo "APP_HOME=${APP_HOME}"
  
  DEPLOY_FILES=(
    "./*.sh"
  )
  echo "DEPLOY_FILES=${DEPLOY_FILES[*]}"
  
  for SVR in ${SVR_LIST[*]}
  do
    scp ${DEPLOY_FILES[*]} root@${SVR}:${APP_HOME}
    ssh root@${SVR} "chmod u+x ${APP_HOME}/*.sh;"
  done
  
  echo "--- (transfer) transfer *.sh files ---"$'\n'
}

## (deploy) deploying trigger
function deploy() {
  echo "+++ (deploy) deploying trigger +++"
  
  case $PROFILE_SYS in
    dev)
      SVR_LIST=(
        '172.28.200.30'
      )
      APP_HOME=/app/WAS/pilot
    ;;
    sta*)
    ;;
  esac
  echo "SVR_LIST=${SVR_LIST[*]}"
  echo "APP_HOME=${APP_HOME}"
  
  for SVR in ${SVR_LIST[*]}
  do
    ssh root@${SVR} "${APP_HOME}/deploy.sh dev"
  done
  
  echo "--- (deploy) deploying trigger ---"$'\n'
}

case $1 in
  build)
    build
    ;;
  trans*)
    transfer
    ;;
  deploy)
    deploy
    ;;
  *)
    build
    transfer
    deploy
    ;;
esac