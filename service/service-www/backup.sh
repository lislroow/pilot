#!/bin/bash

APP_HOME=/app/WAS/pilot
APP_NAME=service-www

LATEST_JAR=$(ls -rt ${APP_HOME}/${APP_NAME}*.jar | tail -n 1)
echo "LATEST_JAR=${LATEST_JAR}"

BACKUP_FILE=$(find ${APP_HOME} -maxdepth 1 -type f ! -newer ${LATEST_JAR} -name '*.jar' ! -samefile ${LATEST_JAR})
echo "BACKUP_FILE=${BACKUP_FILE[*]}"

mv ${BACKUP_FILE[*]} ${APP_HOME}/backup

