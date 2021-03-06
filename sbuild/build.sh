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
#!/bin/sh
scriptHome="`dirname "$0"`"
scriptHome="`cd "$scriptHome"; pwd`"
cd $scriptHome/../;
projectHome=$scriptHome/../

unset SIGMA_EVENTENGINE_HOME
unset SIGMA_EVENTENGINE_CONF_DIR

mvn clean install verify gpg:sign
