#!/bin/bash

## scm
PROJECT_BASE=$PWD

## env
UNAME=`uname -s`
if [[ "$UNAME" = "Linux"* ]]; then
  OS_NAME="linux"
elif [[ "$UNAME" = "CYGWIN"* || "$UNAME" = "MINGW"* ]]; then
  OS_NAME="win"
fi

echo "OS_NAME: $OS_NAME"

case $OS_NAME in
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

## build
MVN_ARGS="${MVN_ARGS} --file ${PROJECT_BASE}/pom.xml"
MVN_ARGS="${MVN_ARGS} -Dfile.encoding=utf-8"
MVN_ARGS="${MVN_ARGS} -Dmaven.test.skip=true"
MVN_ARGS="${MVN_ARGS} --update-snapshots"
MVN_ARGS="${MVN_ARGS} --batch-mode"

mvn $MVN_ARGS clean package spring-boot:repackage

JAR_FILE=`eval $(cat << EOF
  xmlstarlet sel -N x="http://maven.apache.org/POM/4.0.0"
    -t -v 
    "concat(x:project/x:artifactId, '-', x:project/x:version, '.', x:project/x:packaging)"
    ${PROJECT_BASE}/pom.xml;
EOF
)`
JAR_FILE_PTRN=`eval $(cat << EOF
  xmlstarlet sel -N x="http://maven.apache.org/POM/4.0.0"
    -t -v 
    "concat(x:project/x:artifactId, '-*.', x:project/x:packaging)"
    ${PROJECT_BASE}/pom.xml;
EOF
)`

if [ ! -e target/$JAR_FILE ]; then
  echo "build output file '$JAR_FILE' is not found."
fi


## deploy
SVR_LIST=$(cat << EOF
  172.28.200.30
EOF
)

APP_HOME=/app/WAS/proto
DEPLOY_FILES=$(cat << EOF
./target/$JAR_FILE
./start.sh
./stop.sh
EOF
)

PREPARE_CMD=$(cat << EOF
  $APP_HOME/stop.sh;
  find $APP_HOME -maxdepth 1 -type f -name '*.jar' | xargs -i mv {} $APP_HOME/backup;
EOF
)

POST_CMD=$(cat << EOF
  chmod u+x $APP_HOME/*.sh;
  $APP_HOME/start.sh staging;
EOF
)

for SVR in $SVR_LIST
do
  echo "SVR: $SVR"
  
  echo "+++ prepare +++"
  ssh root@$SVR $PREPARE_CMD
  echo "+++ send +++"
  scp $DEPLOY_FILES root@$SVR:$APP_HOME
  echo "+++ post +++"
  ssh root@$SVR $POST_CMD
done
