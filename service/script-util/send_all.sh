#!/bin/bash

PROFILE_SYS_LIST=("dev" "sta")
APP_LIST=("w" "a")

for PROFILE_SYS in ${PROFILE_SYS_LIST[*]}
do
  /z/project/pilot/service/send-script.sh ${PROFILE_SYS}
done

for PROFILE_SYS in ${PROFILE_SYS_LIST[*]}
do
  for APP in ${APP_LIST[*]}
  do
    /z/project/pilot/service/send-conf.sh ${PROFILE_SYS} ${APP}
  done
done

