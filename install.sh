#!/bin/bash


## env
BASEDIR="$( cd $( dirname "$0" ) && pwd -P)"
echo -e "\e[35m+++ [file] ${BASEDIR}/${0##*/} ${@} +++\e[m"
. ${BASEDIR}/sh/include.sh

verboss="false"

PROJECT_BASE="$( cd ${BASEDIR}/.. && pwd -P)"

## (install) install dep-project
function install() {
  echo "+++ (install) install dep-project +++"
  
  tot=${#INSTALL_LIST[*]}
  idx=1
  for project in ${INSTALL_LIST[*]}
  do
    ## build
    mvn_args=""
    mvn_args="${mvn_args} --file ${PROJECT_BASE}/${project}/pom.xml"
    mvn_args="${mvn_args} -Dfile.encoding=utf-8"
    mvn_args="${mvn_args} -Dmaven.test.skip=true"
    mvn_args="${mvn_args} --update-snapshots"
    mvn_args="${mvn_args} --quiet"
    
    mvn_cmd="mvn ${mvn_args} clean install"
    echo -e "## \e[36m[${idx}/${tot}] ${project}:\e[m \e[30;42m${mvn_cmd}\e[m"
    eval "${mvn_cmd}"
    
    idx=$(( $idx + 1 ))
    echo "-----------"
  done
}


ALL_PROJECTS=(
)

PROJECTS=(
  'framework'
  'service/service-lib'
)

argc=$#
argv=("$@")

if [ "${argv[*]}" == "-h" ]; then
  echo "Usage: ${0##*/} [core|online|lib]"
  exit 0;
elif [ "${argv[*]}" == "" ]; then
  INSTALL_LIST=(${PROJECTS[*]})
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

