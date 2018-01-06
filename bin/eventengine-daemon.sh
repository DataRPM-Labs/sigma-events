#-------------------------------------------------------------------------------
# Copyright 2017 DataRPM
# 
# Licensed under the Apache License, Version 2.0 (the "License"); you may not
# use this file except in compliance with the License.  You may obtain a copy
# of the License at
# 
#   http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
# License for the specific language governing permissions and limitations under
# the License.
#-------------------------------------------------------------------------------
#!/bin/bash

USAGE="Usage: eventengine-daemon.sh {start|stop|restart|status}"

if [ -L ${BASH_SOURCE-$0} ]; then
  BIN=$(dirname $(readlink "${BASH_SOURCE-$0}"))
else
  BIN=$(dirname ${BASH_SOURCE-$0})
fi
BIN=$(cd "${BIN}">/dev/null; pwd)

. "${BIN}/common.sh"
. "${BIN}/functions.sh"

HOSTNAME=$(hostname)
SIGMA_EVENTENGINE_NAME="EventEngine"
SIGMA_EVENTENGINE_LOGFILE="${SIGMA_EVENTENGINE_LOG_DIR}/eventengine-${SIGMA_EVENTENGINE_IDENT_STRING}-${HOSTNAME}.log"
SIGMA_EVENTENGINE_OUTFILE="${SIGMA_EVENTENGINE_LOG_DIR}/eventengine-${SIGMA_EVENTENGINE_IDENT_STRING}-${HOSTNAME}.out"
SIGMA_EVENTENGINE_PID="${SIGMA_EVENTENGINE_PID_DIR}/eventengine-${SIGMA_EVENTENGINE_IDENT_STRING}-${HOSTNAME}.pid"
SIGMA_EVENTENGINE_MAIN=com.datarpm.sigma.event.server.EventEngineServer
JAVA_OPTS+=" -Deventengine.log.file=${SIGMA_EVENTENGINE_LOGFILE}"

addJarInDir "${SIGMA_EVENTENGINE_HOME}/server/target/jars"
serverJar=`find ${SIGMA_EVENTENGINE_HOME}/server/target/ -name event-engine-server-*.jar`
SIGMA_EVENTENGINE_CLASSPATH="${serverJar}:$SIGMA_EVENTENGINE_CLASSPATH"

CLASSPATH+=":${SIGMA_EVENTENGINE_CLASSPATH}"

if [[ "${SIGMA_EVENTENGINE_NICENESS}" = "" ]]; then
    export SIGMA_EVENTENGINE_NICENESS=0
fi

function initialize_default_directories() {
  if [[ ! -d "${SIGMA_EVENTENGINE_LOG_DIR}" ]]; then
    echo "Log dir doesn't exist, create ${SIGMA_EVENTENGINE_LOG_DIR}"
    $(mkdir -p "${SIGMA_EVENTENGINE_LOG_DIR}")
  fi

  if [[ ! -d "${SIGMA_EVENTENGINE_PID_DIR}" ]]; then
    echo "Pid dir doesn't exist, create ${SIGMA_EVENTENGINE_PID_DIR}"
    $(mkdir -p "${SIGMA_EVENTENGINE_PID_DIR}")
  fi
}

function wait_for_SIGMA_EVENTENGINE_to_die() {
  local pid
  local count
  pid=$1
  timeout=$2
  count=0
  timeoutTime=$(date "+%s")
  let "timeoutTime+=$timeout"
  currentTime=$(date "+%s")
  forceKill=1

  while [[ $currentTime -lt $timeoutTime ]]; do
    $(kill ${pid} > /dev/null 2> /dev/null)
    if kill -0 ${pid} > /dev/null 2>&1; then
      sleep 3
    else
      forceKill=0
      break
    fi
    currentTime=$(date "+%s")
  done

  if [[ forceKill -ne 0 ]]; then
    $(kill -9 ${pid} > /dev/null 2> /dev/null)
  fi
}

function wait_SIGMA_EVENTENGINE_is_up_for_ci() {
  if [[ "${CI}" == "true" ]]; then
    local count=0;
    while [[ "${count}" -lt 30 ]]; do
      curl -v localhost:8080 2>&1 | grep '200 OK'
      if [[ $? -ne 0 ]]; then
        sleep 1
        continue
      else
        break
      fi
        let "count+=1"
    done
  fi
}

function print_log_for_ci() {
  if [[ "${CI}" == "true" ]]; then
    tail -1000 "${SIGMA_EVENTENGINE_LOGFILE}" | sed 's/^/  /'
  fi
}

function check_if_process_is_alive() {
  local pid
  pid=$(cat ${SIGMA_EVENTENGINE_PID})
  if ! kill -0 ${pid} >/dev/null 2>&1; then
    action_msg "${SIGMA_EVENTENGINE_NAME} process died" "${SET_ERROR}"
    print_log_for_ci
    return 1
  fi
}

function start() {
  local pid

  if [[ -f "${SIGMA_EVENTENGINE_PID}" ]]; then
    pid=$(cat ${SIGMA_EVENTENGINE_PID})
    if kill -0 ${pid} >/dev/null 2>&1; then
      echo "${SIGMA_EVENTENGINE_NAME} is already running"
      return 0;
    fi
  fi

  initialize_default_directories
  nohup nice -n $SIGMA_EVENTENGINE_NICENESS $SIGMA_EVENTENGINE_RUNNER $JAVA_OPTS -cp $CLASSPATH $SIGMA_EVENTENGINE_MAIN >> "${SIGMA_EVENTENGINE_OUTFILE}" 2>&1 < /dev/null &
  pid=$!
  if [[ -z "${pid}" ]]; then
    action_msg "${SIGMA_EVENTENGINE_NAME} start" "${SET_ERROR}"
    return 1;
  else
    action_msg "${SIGMA_EVENTENGINE_NAME} start" "${SET_OK}"
    echo ${pid} > ${SIGMA_EVENTENGINE_PID}
  fi

  wait_SIGMA_EVENTENGINE_is_up_for_ci
  sleep 2
  check_if_process_is_alive
}

function stop() {
  local pid

  if [[ ! -f "${SIGMA_EVENTENGINE_PID}" ]]; then
    echo "${SIGMA_EVENTENGINE_NAME} is not running"
  else
    pid=$(cat ${SIGMA_EVENTENGINE_PID})
    if [[ -z "${pid}" ]]; then
      echo "${SIGMA_EVENTENGINE_NAME} is not running"
    else
      wait_for_SIGMA_EVENTENGINE_to_die $pid 40
      $(rm -f ${SIGMA_EVENTENGINE_PID})
      action_msg "${SIGMA_EVENTENGINE_NAME} stop" "${SET_OK}"
    fi
  fi

  # list all pid that used in remote interpreter and kill them
  for f in ${SIGMA_EVENTENGINE_PID_DIR}/*.pid; do
    if [[ ! -f ${f} ]]; then
      continue;
    fi

    pid=$(cat ${f})
    wait_for_SIGMA_EVENTENGINE_to_die $pid 20
    $(rm -f ${f})
  done

}

function find_SIGMA_EVENTENGINE_process() {
  local pid

  if [[ -f "${SIGMA_EVENTENGINE_PID}" ]]; then
    pid=$(cat ${SIGMA_EVENTENGINE_PID})
    if ! kill -0 ${pid} > /dev/null 2>&1; then
      action_msg "${SIGMA_EVENTENGINE_NAME} running but process is dead" "${SET_ERROR}"
      return 1
    else
      action_msg "${SIGMA_EVENTENGINE_NAME} is running" "${SET_OK}"
    fi
  else
    action_msg "${SIGMA_EVENTENGINE_NAME} is not running" "${SET_ERROR}"
    return 1
  fi
}

case "${1}" in
  start)
    start
    ;;
  stop)
    stop
    ;;
  reload)
    stop
    start
    ;;
  restart)
    stop
    start
    ;;
  status)
    find_SIGMA_EVENTENGINE_process
    ;;
  *)
    echo ${USAGE}
esac
