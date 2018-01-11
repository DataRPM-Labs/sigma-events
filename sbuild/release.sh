#!/bin/bash
scriptHome="`dirname "$0"`"
scriptHome="`cd "$scriptHome"; pwd`"
cd $scriptHome/../;
projectHome=$scriptHome/../

installedXMLStarlet=`which xmlstarlet`
if [[ -z ${installedXMLStarlet} ]]; then
  echo "xmlstarlet missing"
  exit;
fi

projectGroupId=`xmlstarlet sel -N my=http://maven.apache.org/POM/4.0.0 -t -m my:project -v my:groupId ${projectHome}/pom.xml`
modules=(`xmlstarlet sel -N my=http://maven.apache.org/POM/4.0.0 -t -m my:project -v my:modules ${projectHome}/pom.xml`)

releaseVersion=$1
if [ -z ${releaseVersion} ]; then
  echo "USAGE: release.sh releaseNumber {releaseTag} {nextDevRelease}"
  exit;
fi

releaseTag=$2
if [ -z ${releaseTag} ]; then
  releaseTag=$releaseVersion
fi

git push --delete origin $releaseTag
git tag --delete $releaseTag

devReleaseVersion=$3
if [ -z ${devReleaseVersion} ]; then
  devReleaseVersion="${releaseVersion}-SNAPHOST"
fi

batchBuildArg=""
for eachModule in ${modules[@]}; do
 batchBuildArg="${batchBuildArg} -Dproject.rel.${projectGroupId}:${eachModule}=${releaseVersion} -Dproject.dev.${projectGroupId}:${eachModule}=${devReleaseVersion}" 
done

unset SIGMA_EVENTENGINE_HOME
unset SIGMA_EVENTENGINE_CONF_DIR

mvn --batch-mode release:clean release:prepare -Dmaven.test.skip=true -Dtag=$releaseTag -DdevelopmentVersion=$devReleaseVersion ${batchBuildArg} && mvn package gpg:sign release:perform
