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

if [ -L ${BASH_SOURCE-$0} ]; then
  FWDIR=$(dirname $(readlink "${BASH_SOURCE-$0}"))
else
  FWDIR=$(dirname "${BASH_SOURCE-$0}")
fi

if [[ -z "${SIGMA_EVENTENGINE_HOME}" ]]; then
  export SIGMA_EVENTENGINE_HOME="$(cd "${FWDIR}/.."; pwd)"
fi

if [[ -z "${SIGMA_EVENTENGINE_CONF_DIR}" ]]; then
  export SIGMA_EVENTENGINE_CONF_DIR="${SIGMA_EVENTENGINE_HOME}/conf"
fi

if [[ -z "${SIGMA_EVENTENGINE_LOG_DIR}" ]]; then
  export SIGMA_EVENTENGINE_LOG_DIR="${SIGMA_EVENTENGINE_HOME}/logs"
fi

if [[ -z "$SIGMA_EVENTENGINE_PID_DIR" ]]; then
  export SIGMA_EVENTENGINE_PID_DIR="${SIGMA_EVENTENGINE_HOME}/run"
fi

SIGMA_EVENTENGINE_CLASSPATH+=":${SIGMA_EVENTENGINE_CONF_DIR}"

if [[ -z "${SIGMA_EVENTENGINE_ENCODING}" ]]; then
  export SIGMA_EVENTENGINE_ENCODING="UTF-8"
fi

function addEachJarInDir(){
  if [[ -d "${1}" ]]; then
    for jar in $(find -L "${1}" -maxdepth 1 -name '*jar'); do
      SIGMA_EVENTENGINE_CLASSPATH="$jar:$SIGMA_EVENTENGINE_CLASSPATH"
    done
  fi
}

function addJarInDir(){
  if [[ -d "${1}" ]]; then
    SIGMA_EVENTENGINE_CLASSPATH="${1}/*:${SIGMA_EVENTENGINE_CLASSPATH}"
  fi
}

export SIGMA_EVENTENGINE_CLASSPATH

if [[ -z "$SIGMA_EVENTENGINE_MEM" ]]; then
  export SIGMA_EVENTENGINE_MEM="-Xmx1024m -XX:MaxPermSize=512m"
fi

JAVA_OPTS+=" ${SIGMA_EVENTENGINE_JAVA_OPTS} -Dfile.encoding=${SIGMA_EVENTENGINE_ENCODING} ${SIGMA_EVENTENGINE_MEM}"
export JAVA_OPTS

if [[ -n "${JAVA_HOME}" ]]; then
  SIGMA_EVENTENGINE_RUNNER="${JAVA_HOME}/bin/java"
else
  SIGMA_EVENTENGINE_RUNNER=java
fi

export SIGMA_EVENTENGINE_RUNNER

if [[ -z "$SIGMA_EVENTENGINE_IDENT_STRING" ]]; then
  export SIGMA_EVENTENGINE_IDENT_STRING="${USER}"
fi

if [[ -z "$DEBUG" ]]; then
  export DEBUG=0
fi
