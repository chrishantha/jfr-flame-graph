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
    echo "-d: Decompress the JFR file"
    echo "-m: Interval in minutes. Default 10"
    echo "-o: Output Directory. Default \"Output\""
    echo "-i: Ignore line numbers"
    echo "-s: Save folded output"
    echo ""
}

jfr_file=""
minutes=10
output_dir=""
decompress=""
ignore_lines=""
save_folded_output=false

while getopts "df:m:io:s" opts
do
  case $opts in
    f)
        jfr_file=${OPTARG}
        ;;
    d)
        decompress="-d"
        ;;
    m)
        minutes=${OPTARG}
        ;;
    i)
        ignore_lines="-i"
        ;;
    o)
        output_dir=${OPTARG}
        ;;
    s)
        save_folded_output=true
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
if [[ ! -d $output_dir ]]; then
    output_dir="output"
    mkdir -p $output_dir
fi

#Validate output directory
if [[ ! -d $output_dir ]]; then
    echo "Please specify a directory to create the flamegraphs"
    exit 1
fi

jfr_filename=$(basename $jfr_file)

details=$(${JFG_DIR}/flamegraph-output.sh -ot folded $decompress -f $jfr_file -j -t)

echo "$details"

startTimestamp=$(echo $details | sed -r 's/.*Min Start Event\s*: ([0-9]*).*/\1/')
endTimestamp=$(echo $details | sed -r 's/.*Max End Event\s*: ([0-9]*).*/\1/')

interval=$(($minutes * 60))

i=$startTimestamp
end=$endTimestamp

dateformat="%Y-%m-%d %I:%M:%S %p"

set +e

while [ $i -lt $end ]; do
    s=$i
    i=$(($i+$interval))
    e=$i

    if [ $e -gt $end ]; then
        e=$end
    fi

    title="Flame Graph for $jfr_filename from $(date --date @$s +"$dateformat") to $(date --date @$e +"$dateformat")"

    echo Generating $title

    output_file=flamegraph-$s-$e.svg

    # Use folded output type
    flamegraph_output_command="${JFG_DIR}/flamegraph-output.sh"
    flamegraph_output_args=(-ot folded $decompress -f $jfr_file -st $s -et $e $ignore_lines)
    framegraph_generate_command="$FLAMEGRAPH_DIR/flamegraph.pl"
    framegraph_generate_args=(--title "$title" --width 1600)

    if [[ "$save_folded_output" = true ]]; then
        $($flamegraph_output_command "${flamegraph_output_args[@]}" > $output_dir/$output_file.folded)
        cat $output_dir/$output_file.folded | $framegraph_generate_command "${framegraph_generate_args[@]}" > $output_dir/$output_file
    else
        $flamegraph_output_command "${flamegraph_output_args[@]}" | $framegraph_generate_command "${framegraph_generate_args[@]}" > $output_dir/$output_file
    fi

    flamegraph_status=("${PIPESTATUS[@]}")
    if [ ${flamegraph_status[1]} -eq 0 ]
    then
        # Create array
        output_files+=($output_file)
    else
        rm $output_dir/$output_file
    fi
done

#Generate HTML
index_file=$output_dir/index.html

cat << _EOF_ > $index_file
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Flame Graphs</title>
</head>
<body>

<div>
    <button onclick="plusDivs(-1)">Previous</button>
    <button onclick="plusDivs(1)">Next</button>
_EOF_
for f in "${output_files[@]}"
do
	echo "<object class=\"flamegraph\" data=\"$f\" type=\"image/svg+xml\" style=\"display: none\"></object>" >> $index_file
done
cat << _EOF_ >> $index_file
</div>

<script>
    var slideIndex = 1;
    showDivs(slideIndex);

    function plusDivs(n) {
        showDivs(slideIndex += n);
    }

    function showDivs(n) {
        var i;
        var x = document.getElementsByClassName("flamegraph");
        if (n > x.length) {
            slideIndex = 1
        }
        if (n < 1) {
            slideIndex = x.length
        }
        for (i = 0; i < x.length; i++) {
            x[i].style.display = "none";
        }
        x[slideIndex - 1].style.display = "block";
    }
</script>

</body>
</html>
_EOF_

echo Script executed in $SECONDS seconds.