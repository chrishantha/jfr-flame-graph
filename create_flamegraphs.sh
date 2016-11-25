#!/bin/bash
# Copyright 2016 M. Isuru Tharanga Chrishantha Perera
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
# Create multiple flame graphs
# ----------------------------------------------------------------------------
set -e
#set -x

JFG_DIR=$(dirname "$0")

if [ ! -x "$FLAMEGRAPH_DIR/flamegraph.pl" ]; then
  echo "Please clone https://github.com/brendangregg/FlameGraph and set FLAMEGRAPH_DIR to the root directory"
  exit 1
fi

function help {
    echo ""
    echo "Usage: "
    echo "create-flamegraphs.sh [options]"
    echo ""
    echo "Options:"
    echo "-f: JFR file"
    echo "-m: Interval in minutes. Default 10"
    echo "-o: Output Directory. Default \"Output\""
    echo ""
}

jfr_file=""
minutes=10
output_dir=""
decompress=""
ignore_lines=""

while getopts "df:m:o:airs" opts
do
  case $opts in
    f)
        jfr_file=${OPTARG}
        ;;
    m)
        minutes=${OPTARG}
        ;;
    i)
        ignore_lines="-i"
        ;;
    \?)
        help
        exit 1
        ;;
  esac
done

if [[ ! -f $jfr_file ]]; then
    echo "Please specify the JFR file"
    help
    exit 1
fi

#If no directory was provided, we need to create the default one
if [[ ! -d $java_dir ]]; then
    output_dir="output"
    mkdir -p $output_dir
fi

#Validate output directory
if [[ ! -d $output_dir ]]; then
    echo "Please specify a directory to create the flamegraphs"
    exit 1
fi

jfr_filename=$(basename $jfr_file)

details=$(${JFG_DIR}/flamegraph-output.sh folded -f $jfr_file -j -t)

echo "$details"

startTimestamp=$(echo $details | sed -r 's/.*Start : ([0-9]*).*/\1/')
endTimestamp=$(echo $details | sed -r 's/.*End : ([0-9]*).*/\1/')

interval=$(($minutes * 60))

i=$startTimestamp
end=$endTimestamp

dateformat="%Y-%m-%d %I:%M:%S %p"

set +e

# All Output Files
output_files

while [ $i -lt $end ]; do
    s=$i
    i=$(($i+$interval))
    e=$i

    if [ $e -gt $end ]; then
        e=$end
    fi

    title=$(echo Flame Graph for $jfr_filename from $(date --date @$s +"$dateformat") to $(date --date @$e +"$dateformat"))

    echo Generating $title

    output_file=$output_dir/flamegraph-$s-$e.svg

    # Use folded command
    ${JFG_DIR}/flamegraph-output.sh folded -f $jfr_file -x $s -y $e | \
    $FLAMEGRAPH_DIR/flamegraph.pl --title "$title" --width 1600 \
    > $output_file

    flamegraph_status=("${PIPESTATUS[@]}")
    if [ ${flamegraph_status[1]} -eq 0 ]
    then
        output_files+=($output_file)
    else
        rm $output_file
    fi
done
