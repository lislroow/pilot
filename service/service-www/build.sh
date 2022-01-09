#!/bin/bash

## env
PROJECT_BASE=$PWD
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
    ;;
  win)
    M2_HOME=/z/develop/build/maven-3.6.3
    JAVA_HOME=/z/develop/java/openjdk-11.0.13.8-temurin
    PATH=$JAVA_HOME/bin:$M2_HOME/bin:$PATH
    MD5SUM_CMD=md5sum.exe
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

## build
MVN_ARGS=""
MVN_ARGS="${MVN_ARGS} --file ${PROJECT_BASE}/pom.xml"
MVN_ARGS="${MVN_ARGS} -Dfile.encoding=utf-8"
MVN_ARGS="${MVN_ARGS} -Dmaven.test.skip=true"
MVN_ARGS="${MVN_ARGS} --update-snapshots"
MVN_ARGS="${MVN_ARGS} --batch-mode"
MVN_ARGS="${MVN_ARGS} --quiet"

MVN_CMD="mvn $MVN_ARGS clean package spring-boot:repackage"
echo "${MVN_CMD}"
eval "${MVN_CMD}"

ARTIFACT_FILE=`eval $(cat << EOF
  xmlstarlet sel -N x="http://maven.apache.org/POM/4.0.0"
    -t -v 
    "concat(x:project/x:artifactId, '-', x:project/x:version, '.', x:project/x:packaging)"
    ${PROJECT_BASE}/pom.xml;
EOF
)`

CHECKSUM=$(${MD5SUM_CMD} target/${ARTIFACT_FILE} | awk '{ print substr($1, 1, 4) }')
JAR_FILE=${ARTIFACT_FILE%.*}_${CHECKSUM}.${ARTIFACT_FILE##*.}
cp target/${ARTIFACT_FILE} target/${JAR_FILE}

echo "ARTIFACT_FILE=${ARTIFACT_FILE}"
echo "JAR_FILE=${JAR_FILE}"

JAR_FILE_PTRN=`eval $(cat << EOF
  xmlstarlet sel -N x="http://maven.apache.org/POM/4.0.0"
    -t -v 
    "concat(x:project/x:artifactId, '-*.', x:project/x:packaging)"
    ${PROJECT_BASE}/pom.xml;
EOF
)`
echo "JAR_FILE_PTRN=${JAR_FILE_PTRN}"

if [ ! -e target/$JAR_FILE ]; then
  echo "build output file '$JAR_FILE' is not found."
fi

## deploy-nexus
MVN_ARGS=""
MVN_ARGS="${MVN_ARGS} -DpomFile=${PROJECT_BASE}/pom.xml"
MVN_ARGS="${MVN_ARGS} -Dfile=${PROJECT_BASE}/target/${JAR_FILE}"
MVN_ARGS="${MVN_ARGS} --quiet"

# jar 파일명에 "-SNAPSHOT" 이 있으면 snapshot 저장소에 deploy 되어야 합니다. 
if [[ "${JAR_FILE}" = *"-SNAPSHOT"* ]]; then
  MVN_ARGS="${MVN_ARGS} -DrepositoryId=maven-snapshot"
  MVN_ARGS="${MVN_ARGS} -Durl=http://nexus/repository/maven-snapshot/"
else
  MVN_ARGS="${MVN_ARGS} -DrepositoryId=maven-release"
  MVN_ARGS="${MVN_ARGS} -Durl=http://nexus/repository/maven-release/"
fi

MVN_CMD="mvn deploy:deploy-file $MVN_ARGS"
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
  "./target/${JAR_FILE}"
  "./start-*.sh"
  "./stop-*.sh"
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
  ssh root@${SVR} "${APP_HOME}/stop-dwww11.sh"
  ssh root@${SVR} "${APP_HOME}/start-dwww11.sh"
  
  ssh root@${SVR} "${APP_HOME}/stop-dwww12.sh"
  ssh root@${SVR} "${APP_HOME}/start-dwww12.sh"
  echo "=== //restart www11 / www12 ==="
  
done
