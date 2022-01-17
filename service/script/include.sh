
shopt -s extglob

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
    LOCAL_IP=$(ip -4 addr show ens33 | grep -oP '(?<=inet\s)\d+(\.\d+){3}')
    ;;
  win)
    JAVA_HOME=/z/develop/java/openjdk-11.0.13.8-temurin
    M2_HOME=/z/develop/build/maven-3.6.3
    PATH=$JAVA_HOME/bin:$M2_HOME/bin:$PATH
    LOCAL_IP="localhost"
    ;;
  *)
    echo "invalid os"
    exit -1
    ;;
esac

printf '%s\n' $(cat << EOF
UNAME=${UNAME}
JAVA_HOME=${JAVA_HOME}
M2_HOME=${M2_HOME}
EOF
)

DOMAIN="pilot"
PARENT_DIR=${BASEDIR##*/}
PROFILE_SYS=${PARENT_DIR:(${#DOMAIN}+1):1}

EXEC_USER="tomcat"

readonly SVR_INFO=(
  'pilot-www|dwww11|/app/pilot-dev|dev|172.28.200.30|7100'
  'pilot-www|dwww12|/app/pilot-dev|dev|172.28.200.30|7101'
  'pilot-adm|dadm11|/app/pilot-dev|dev|172.28.200.30|7200'
  'pilot-adm|dadm12|/app/pilot-dev|dev|172.28.200.30|7201'
  'pilot-www|swww11|/app/pilot-sta|sta|172.28.200.30|9100'
  'pilot-www|swww12|/app/pilot-sta|sta|172.28.200.30|9101'
  'pilot-adm|sadm11|/app/pilot-sta|sta|172.28.200.30|9200'
  'pilot-adm|sadm12|/app/pilot-sta|sta|172.28.200.30|9201'
)

function GetSvrInfo() {
  local field=$1
  local key1=$2
  local val1=$3
  local key2=$4
  local val2=$5
  local list=()
  for row in ${SVR_INFO[@]}
  do
    local app_name app_id app_home profile_sys ip port
    IFS=$'|'; read -r app_name app_id app_home profile_sys ip port <<< "$row"
    if [ "${key2}" == "" ]; then
      if [[ "${key1}" == "ALL" || "${!key1}" == *"${val1}"* ]]; then
        list+=("${!field}")
      else
        continue
      fi
    else
      if [[ "${key1}" == "ALL" || ( "${!key1}" == *"${val1}"* && "${!key2}" == *"${val2}"* ) ]]; then
        if [[ "${OS_NAME}" == "win" || "${LOCAL_IP}" == "${ip}" ]]; then
          list+=("${!field}")
        fi
      else
        continue
      fi
    fi
  done
  unset IFS;
  
  ulist=($(printf '%s\n' "${list[@]}" | sort -u))
  echo "${ulist[@]}"
}

function ExecCmd() {
  # parameter "./say.sh hello"        > $# : 2
  # parameter "./say.sh hello world " > $# : 3
  # execCmd is array
  echo "$#"
  echo "$@"
  local execCmd=$@
  if [ $(whoami) == "root" ]; then
    # execCmd[@]:   (X)
    # execCmd[*]:   (X)
    # "execCmd[*]": (O)
    su ${EXEC_USER} -c "${execCmd[*]}"
  elif [ $(whoami) == ${EXEC_USER} ]; then
    # ${execCmd[*]}:        (X)
    # eval "${execCmd[*]}": (O)
    eval "${execCmd[*]}"
  fi
}




