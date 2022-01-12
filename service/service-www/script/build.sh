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
    MD5SUM_CMD=md5sum
    DIRNAME_CMD=dirname
    ;;
  win)
    M2_HOME=/z/develop/build/maven-3.6.3
    JAVA_HOME=/z/develop/java/openjdk-11.0.13.8-temurin
    PATH=$JAVA_HOME/bin:$M2_HOME/bin:$PATH
    MD5SUM_CMD=md5sum.exe
    DIRNAME_CMD=dirname.exe
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

## env
PROJECT_BASE="$( cd $( ${DIRNAME_CMD} "$0" )/.. && pwd -P)"
echo "${PROJECT_BASE}"

PROFILE_SYS=$1

if [ "${PROFILE_SYS}" = "" ]; then
  PROFILE_SYS=dev
fi

case ${PROFILE_SYS} in
  dev|sta*)
    ;;
  *)
    echo "PROFILE_SYS('${PROFILE_SYS}') is not yet!"
    exit -1
esac

## build
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



## deploy-was
SVR_LIST=(
  '172.28.200.30'
)
echo "SVR_LIST=${SVR_LIST[*]}"

APP_HOME=/app/WAS/pilot
echo "APP_HOME=${APP_HOME}"

DEPLOY_FILES=(
  "${PROJECT_BASE}/target/${JAR_MD5_FILE}"
  "${PROJECT_BASE}/script/start*.sh"
  "${PROJECT_BASE}/script/stop*.sh"
  "${PROJECT_BASE}/script/status.sh"
  "${PROJECT_BASE}/script/backup.sh"
)
echo "DEPLOY_FILES=${DEPLOY_FILES[*]}"

for SVR in ${SVR_LIST[*]}
do
  echo "=== send DEPLOY_FILES ==="
  echo "DEPLOY_FILES=${DEPLOY_FILES[*]}"
  scp ${DEPLOY_FILES[*]} root@${SVR}:${APP_HOME}
  ssh root@${SVR} "chmod u+x ${APP_HOME}/*.sh;"
  echo "=== //send DEPLOY_FILES ==="
  
  echo "=== restart www11 / www12 ==="
  ssh root@${SVR} "${APP_HOME}/stopdwww11.sh"
  ssh root@${SVR} "${APP_HOME}/startdwww11.sh"
  
  ssh root@${SVR} "${APP_HOME}/stopdwww12.sh"
  ssh root@${SVR} "${APP_HOME}/startdwww12.sh"
  echo "=== //restart www11 / www12 ==="
  
done
