[![Build Status](https://travis-ci.org/chrishantha/jfr-flame-graph.svg?branch=master)](https://travis-ci.org/chrishantha/jfr-flame-graph)

Converting JFR Method Profiling Samples to FlameGraph compatible format.
========================================================================

This is a simple application to read Method Profiling Samples from Java Flight Recorder dump and convert those Stack Traces to [FlameGraph] compatible format.
[FlameGraph]: https://github.com/brendangregg/FlameGraph

This application uses the unsupported [JMC Parser].
[JMC Parser]: http://hirt.se/blog/?p=446

See my blog post on "[Flame Graphs with Java Flight Recordings]" for more details.

[Flame Graphs with Java Flight Recordings]: http://isuru-perera.blogspot.com/2015/05/flame-graphs-with-java-flight-recordings.html

## How to build

**Step 1:**

Run `install-mc-jars.sh` script.

> The required JMC dependencies need to be installed to a local repository first. The script will install required JMC jars to the local repository (The `repo` directory) and output a file named `jmc_version.properties`, which will show you the version of Java Mission Control Dependencies used.
> This script should automatically update the `<jmc.version>` property in `pom.xml`. Please verify that the `<jmc.version>` value is equal to the version found in `jmc_version.properties`.

**Step 2:**

Run `mvn clean install -U`.

## Clone FlameGraph repository

Clone [Brendan]'s [FlameGraph] repository and set the environment variable `FLAMEGRAPH_DIR` to FlameGraph directory

[Brendan]: http://www.brendangregg.com/bio.html

```
git clone https://github.com/brendangregg/FlameGraph.git
export FLAMEGRAPH_DIR=/path/to/FlameGraph
```

## How to generate a Flame Graph

There are helper scripts, to generate the flame graphs.

For example:

```
./create_flamegraph.sh -f /tmp/highcpu.jfr -i > flamegraph.svg
```
Open the SVG file in your web browser.

Use -h with scripts to see the available options.

For example:
```
$ ./flamegraph-output.sh -h
  Usage: Application [options] [command] [command options]
    Options:
      -h, --help
        Display Help
        Default: false
    Commands:
      folded      Create folded output
        Usage: folded [options]
          Options:
            -d, --decompress
              Decompress the JFR file
              Default: false
            -y, --end-timestamp
              End timestamp in seconds for filtering
              Default: 0
            -a, --hide-arguments
              Hide arguments in methods
              Default: false
            -i, --ignore-line-numbers
              Ignore Line Numbers in Stack Frame
              Default: false
          * -f, --jfrdump
              Java Flight Recorder Dump
            -o, --output
              Output file
            -j, --print-jfr-details
              Print JFR details and exit
              Default: false
            -t, --print-timestamp
              Print timestamp in JFR Details
              Default: false
            -r, --show-return-value
              Show return value for methods in the stack
              Default: false
            -x, --start-timestamp
              Start timestamp in seconds for filtering
              Default: 0
            -s, --use-simple-names
              Use simple names instead of qualified names in the stack
              Default: false
  
      json      Create json output for d3-flame-graph
        Usage: json [options]
          Options:
            -d, --decompress
              Decompress the JFR file
              Default: false
            -y, --end-timestamp
              End timestamp in seconds for filtering
              Default: 0
            -a, --hide-arguments
              Hide arguments in methods
              Default: false
            -i, --ignore-line-numbers
              Ignore Line Numbers in Stack Frame
              Default: false
          * -f, --jfrdump
              Java Flight Recorder Dump
            -l, --live
              Export stack trace sample timestamp
              Default: false
            -o, --output
              Output file
            -j, --print-jfr-details
              Print JFR details and exit
              Default: false
            -t, --print-timestamp
              Print timestamp in JFR Details
              Default: false
            -r, --show-return-value
              Show return value for methods in the stack
              Default: false
            -x, --start-timestamp
              Start timestamp in seconds for filtering
              Default: 0
            -s, --use-simple-names
              Use simple names instead of qualified names in the stack
              Default: false
```

## License

Copyright (C) 2015 M. Isuru Tharanga Chrishantha Perera

Licensed under the Apache License, Version 2.0
