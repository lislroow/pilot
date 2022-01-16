#!/bin/bash

echo "### [start] ${0##*/} ${@} ###"

## env
echo "+++ (system-env) +++"
BASEDIR="$( cd $( dirname "$0" ) && pwd -P)"

## include
. ${BASEDIR}/script/include.sh

## (build) build maven project
function build() {
  echo "+++ (build) build maven project +++"
  
  case "$1" in
    all)
      read -ra app_name_arr <<< $(GetSvrInfo "app_name" "ALL")
      ;;
    @(w|a)?(ww|dm))
      read -ra app_name_arr <<< $(GetSvrInfo "app_name" "app_name" "$1")
      ;;
  esac
  
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
    
    mvn_cmd="mvn ${mvn_args} deploy:deploy-file"
    echo "mvn_cmd=${mvn_cmd}"
    eval "${mvn_cmd}"
  done
  
  echo "--- //(build) build maven project ---"
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


echo "### [finish] ${0##*/} ${@} ###"$'\n'$'\n'
