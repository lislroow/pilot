#!/bin/bash

## env
BASEDIR="$( cd $( dirname "$0" ) && pwd -P)"
echo -e "\e[35m+++ [file] ${BASEDIR}/${0##*/} ${@} +++\e[m"
. ${BASEDIR}/sh/include.sh

verboss="false"

function build() {
  echo "+++ [func] ${BASEDIR}/${0##*/}:$FUNCNAME: build maven project +++"
  
  case "$1" in
    all)
      read -ra app_name_arr <<< $(GetSvrInfo "app_name" "ALL")
      ;;
    @(w|a)?(ww|dm))
      read -ra app_name_arr <<< $(GetSvrInfo "app_name" "app_name" "$1")
      ;;
  esac
  
  tot=$((${#app_name_arr[@]}))
  idx=1
  for app_name in ${app_name_arr[@]}
  do
    local mvn_args=""
    mvn_args="${mvn_args} --file ${BASEDIR}/${app_name}/pom.xml"
    mvn_args="${mvn_args} -Dfile.encoding=utf-8"
    mvn_args="${mvn_args} -Dmaven.test.skip=true"
    mvn_args="${mvn_args} --update-snapshots"
    mvn_args="${mvn_args} --batch-mode"
    mvn_args="${mvn_args} --quiet"
    
    local mvn_cmd="mvn ${mvn_args} clean package spring-boot:repackage"
    echo -e "## \e[36m[${idx}/${tot}] ${app_name}:\e[m \e[44m${mvn_cmd}\e[m"
    eval "${mvn_cmd}"
    
    local jar_file=$(xmlstarlet sel -N x="http://maven.apache.org/POM/4.0.0" \
      -t -v \
      "concat(x:project/x:artifactId, '-', x:project/x:version, '.', x:project/x:packaging)" \
      ${BASEDIR}/${app_name}/pom.xml)
    echo "jar_file=${jar_file}"
    
    # jar 파일명에 "-SNAPSHOT" 이 있으면 snapshot 저장소에 deploy 되어야 합니다.
    local nx_repo_id 
    if [[ "${jar_file}" = *"-SNAPSHOT"* ]]; then
      nx_repo_id="maven-snapshot"
    else
      nx_repo_id="maven-release"
    fi
    
    ## deploy-nexus
    mvn_args=""
    mvn_args="${mvn_args} -DpomFile=${BASEDIR}/${app_name}/pom.xml"
    mvn_args="${mvn_args} -Dfile=${BASEDIR}/${app_name}/target/${jar_file}"
    mvn_args="${mvn_args} --quiet"
    mvn_args="${mvn_args} -DrepositoryId=${nx_repo_id}"
    mvn_args="${mvn_args} -Durl=https://nexus/repository/${nx_repo_id}/"
    
    mvn_cmd="mvn ${mvn_args} deploy:deploy-file"
    echo -e "## \e[36m[${idx}/${tot}] ${app_name}:\e[m \e[44m${mvn_cmd}\e[m"
    eval "${mvn_cmd}"
    idx=$(( $idx + 1 ))
  done
}



case "$1" in
  all)
    ;;
  @(w|a)?(ww|dm))
    ;;
  *)
    echo "Usage: ${0##*/} [all|w|a]"
    exit 0;
    ;;
esac



build "$1";


