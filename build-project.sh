#!/bin/bash


## env
BASEDIR="$( cd $( dirname "$0" ) && pwd -P)"
echo -e "\e[35m+++ [file] ${BASEDIR}/${0##*/} ${@} +++\e[m"
. ${BASEDIR}/sh/include.sh

verboss="false"

PROJECT_BASE="$( cd ${BASEDIR}/.. && pwd -P)"

function build_snapshot() {
  echo "+++ [func] ${BASEDIR}/${0##*/}:$FUNCNAME: build_snapshot +++"
  project=$1
  
  local mvn_args=""
  mvn_args="${mvn_args} --file ${BASEDIR}/${project}/pom.xml"
  mvn_args="${mvn_args} -Dfile.encoding=utf-8"
  mvn_args="${mvn_args} -Dmaven.test.skip=true"
  mvn_args="${mvn_args} --update-snapshots"
  mvn_args="${mvn_args} --batch-mode"
  #mvn_args="${mvn_args} --quiet"
  
  mvn_cmd="mvn $mvn_args clean deploy"
  echo "${mvn_cmd}"
  eval "${mvn_cmd}"
}

function build_release() {
  echo "+++ [func] ${BASEDIR}/${0##*/}:$FUNCNAME: build_release +++"
  project=$1
  
  local mvn_args=""
  mvn_args="${mvn_args} --file ${BASEDIR}/${project}/pom.xml"
  mvn_args="${mvn_args} -Dfile.encoding=utf-8"
  mvn_args="${mvn_args} -Dmaven.test.skip=true"
  mvn_args="${mvn_args} --update-snapshots"
  mvn_args="${mvn_args} --batch-mode"
  #mvn_args="${mvn_args} --quiet"
  local framework_ver=$(GetNxVer "https://nexus/repository/maven-release/mgkim/framework/framework-bom")
  
  mvn_cmd="mvn $mvn_args -Prelease clean release:clean release:prepare release:perform -Darguments=\"-Dframework.version=${framework_ver}\""
  echo "${mvn_cmd}"
  eval "${mvn_cmd}"
}

## (build_lib) 
function build_project() {
  echo "+++ [func] ${BASEDIR}/${0##*/}:$FUNCNAME: build_project +++"
  
  local mvn_goals=()
  case "$1" in
    all)
      mvn_goals+=("snapshot")
      mvn_goals+=("release")
      ;;
    d*)
      mvn_goals+=("snapshot")
      ;;
    s*)
      mvn_goals+=("release")
      ;;
    *)
      ;;
  esac
  
  for project in ${PROJECTS[*]}
  do
    for mvn_goal in ${mvn_goals[*]}
    do
      case "${mvn_goal}" in
        snapshot)
          build_snapshot "${project}"
        ;;
        release)
          build_release "${project}"
        ;;
      esac
      #if [ "${mvn_goal}" == "snapshot" ]; then
      #elif [ "${mvn_goal}" == "release" ]; then
      #  build_release "${project}"
      #  #GIT_PUSH="git push origin ${PROJECT_BASE}"
      #  #echo "${mvn_cmd}"
      #  #eval "${mvn_cmd}"
      #fi
    done
  done
}

PROJECTS=(
  'framework'
  'service/service-lib'
)

argc=$#
argv=("$@")

if [ "${argv[*]}" == "-h" ]; then
  echo "Usage: ${0##*/} [all|d|s]"
  exit 0;
fi

build_project "$1";


echo "### [finish] ${0##*/} ${@} ###"$'\n'$'\n'

