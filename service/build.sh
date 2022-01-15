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

printf '%s\n' $(cat << EOF
BASEDIR=${BASEDIR}
UNAME=${UNAME}
OS_NAME=${OS_NAME}
JAVA_HOME=${JAVA_HOME}
M2_HOME=${M2_HOME}
EOF
)



## (build) build maven project
function build() {
  echo "+++ (build) build maven project +++"
  
  for app_name in ${APP_NAME_LIST[*]}
  do
    local mvn_args=""
    mvn_args="${mvn_args} --file ${BASEDIR}/${app_name}/pom.xml"
    mvn_args="${mvn_args} -Dfile.encoding=utf-8"
    mvn_args="${mvn_args} -Dmaven.test.skip=true"
    mvn_args="${mvn_args} --update-snapshots"
    mvn_args="${mvn_args} --batch-mode"
    mvn_args="${mvn_args} --quiet"
    
    local mvn_cmd="mvn ${mvn_args} clean package spring-boot:repackage"
    echo "mvn_cmd=${mvn_cmd}"
    eval "${mvn_cmd}"
    
    local jar_file=$(xmlstarlet sel -N x="http://maven.apache.org/POM/4.0.0" \
      -t -v \
      "concat(x:project/x:artifactId, '-', x:project/x:version, '.', x:project/x:packaging)" \
      ${BASEDIR}/${app_name}/pom.xml)
    echo "jar_file=${jar_file}"
    
    # jar 파일명에 "-SNAPSHOT" 이 있으면 snapshot 저장소에 deploy 되어야 합니다.
    local nx_repo 
    if [[ "${jar_file}" = *"-SNAPSHOT"* ]]; then
      nx_repo="snapshot"
    else
      nx_repo="release"
    fi
    
    ## deploy-nexus
    mvn_args=""
    mvn_args="${mvn_args} -DpomFile=${BASEDIR}/${app_name}/pom.xml"
    mvn_args="${mvn_args} -Dfile=${BASEDIR}/${app_name}/target/${jar_file}"
    mvn_args="${mvn_args} --quiet"
    mvn_args="${mvn_args} -DrepositoryId=maven-${nx_repo}"
    mvn_args="${mvn_args} -Durl=https://nexus/repository/maven-${nx_repo}/"
    
    mvn_cmd="mvn deploy:deploy-file ${mvn_args}"
    echo "mvn_cmd=${mvn_cmd}"
    eval "${mvn_cmd}"
  done
  
  echo "--- //(build) build maven project ---"
}


echo "+++ (runtime-env) +++"
APP_NAME_LIST=()
case "$1" in
  *w*)
    APP_NAME_LIST+=("pilot-www")
    ;;
  *a*)
    APP_NAME_LIST+=("pilot-adm")
    ;;
  -h)
    echo "Usage: ${0##*/} [w|a]"
    exit 0;
    ;;
  *)
    APP_NAME_LIST+=(
      "pilot-www"
      "pilot-adm"
    )
    ;;
esac


printf '%s\n' $(cat << EOF
APP_NAME_LIST=${APP_NAME_LIST[*]}
EOF
)


build;


echo "### [finish] ${0##*/} ${@} ###"$'\n'$'\n'
