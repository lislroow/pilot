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



## (install) install dep-project
function install() {
  echo "+++ (install) install dep-project +++"
  
  tot=${#INSTALL_LIST[*]}
  idx=0
  for PROJECT in ${INSTALL_LIST[*]}
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
    echo "[${idx}/${tot}] ${MVN_CMD}"
    eval "${MVN_CMD}"
  done
  
  echo "+++ (install) install dep-project +++"
}


echo "+++ (runtime-env) +++"
ALL_PROJECTS=(
  'bom/framework-bom'
  'bom/service-bom'
  'framework/framework-batch' 
  'framework/framework-core'
  'framework/framework-daemon'
  'framework/framework-online'
  'service/service-lib'
)

argc=$#
argv=("$@")
#echo "argc=${argc}"
#echo "argv=${argv[*]}"

if [ "${argv[*]}" == "-h" ]; then
  echo "Usage: ${0##*/} [core|online|lib]"
  exit 0;
elif [ "${argv[*]}" == "" ]; then
  INSTALL_LIST=(${ALL_PROJECTS[*]})
else
  for ((i=0; i<argc; i++)); do
    echo "argv[$i]=${argv[$i]}"
    
    case "${argv[$i]}" in
      *core)
        INSTALL_LIST+=("framework/framework-core")
        ;;
      *online)
        INSTALL_LIST+=("framework/framework-online")
        ;;
      *lib)
        INSTALL_LIST+=("service/service-lib")
        ;;
    esac
  done
fi

install;


echo "### [finish] ${0##*/} ${@} ###"$'\n'$'\n'

