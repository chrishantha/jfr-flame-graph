#!/bin/bash
# Copyright 2015 M. Isuru Tharanga Chrishantha Perera
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# ----------------------------------------------------------------------------
# Installing JMC Jars to a local repo
# ----------------------------------------------------------------------------

jmc_version=`echo $(basename $JAVA_HOME/lib/missioncontrol/plugins/com.jrockit.mc.common_*.jar) | sed 's/.*_\(.*\)\.jar/\1/'`

echo "JMC JAR version: $jmc_version"

echo "jmc.version=$jmc_version" > jmc_version.properties

function install_jar() {
    jar=$1_$jmc_version.jar
    echo "Installing JAR $jar"
    mvn install:install-file -DlocalRepositoryPath=repo -DcreateChecksum=true -Dpackaging=jar -Dfile=$JAVA_HOME/lib/missioncontrol/plugins/$jar -DgroupId=com.jrockit.mc -DartifactId=$1 -Dversion=$jmc_version
}

install_jar com.jrockit.mc.common
install_jar com.jrockit.mc.flightrecorder

sed -i -e "/<properties>/,/<\/properties>/ s|<jmc.version>.*</jmc.version>|<jmc.version>$jmc_version</jmc.version>|g" pom.xml