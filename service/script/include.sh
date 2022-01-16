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
  for row in ${SVR_INFO[*]}
  do
    local app_name app_id app_home profile_sys ip port
    IFS=$'|'; read -r app_name app_id app_home profile_sys ip port <<< "$row"
    if [ "${key2}" == "" ]; then
      if [[ "${key1}" == "ALL"  ||  "${!key1}" == *"${val1}"* ]]; then
        list+=("${!field}")
      else
        continue
      fi
    else
      if [[ "${key1}" == "ALL"  || ( "${!key1}" == *"${val1}"* && "${!key2}" == *"${val2}"* ) ]]; then
        list+=("${!field}")
      else
        continue
      fi
    fi
  done
  unset IFS;
  
  ulist=($(printf '%s\n' "${list[@]}" | sort -u))
  echo "${ulist[@]}"
}