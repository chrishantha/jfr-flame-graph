Note: Travis has removed the support for Oracle JDK 8. Therefore the build status is removed temporarily.

Converting JFR Method Profiling Samples to FlameGraph compatible format.
========================================================================

This is a simple application to read Method Profiling Samples from Java Flight Recorder dump and convert those Stack Traces to [FlameGraph] compatible format.

[FlameGraph]: https://github.com/brendangregg/FlameGraph

This application uses the unsupported [JMC Parser].

[JMC Parser]: http://hirt.se/blog/?p=446

See my blog post on "[Flame Graphs with Java Flight Recordings]" for more details.

[Flame Graphs with Java Flight Recordings]: http://isuru-perera.blogspot.com/2015/05/flame-graphs-with-java-flight-recordings.html

## Prerequisites

This project depends on Oracle JDK 8. Therefore, make sure that `JAVA_HOME` is set to Oracle JDK 8.

## How to build and install

Build and install `jfr-flame-graph` app using

```
./gradlew installDist
```

This will install the executable into `./build/install/jfr-flame-graph/bin`.

You can add this location to your `PATH`.

## Clone FlameGraph repository

Clone [Brendan]'s [FlameGraph] repository and set the environment variable `FLAMEGRAPH_DIR` to FlameGraph directory

[Brendan]: http://www.brendangregg.com/bio.html

```
git clone https://github.com/brendangregg/FlameGraph.git
export FLAMEGRAPH_DIR=/path/to/FlameGraph
```

## How to generate a Flame Graph

There are helper scripts, to generate the flame graphs in `./build/install/jfr-flame-graph/bin` directory.

For example:

```
./create_flamegraph.sh -f /tmp/highcpu.jfr -i > flamegraph.svg
```
Open the SVG file in your web browser.

Use -h with scripts to see the available options.

For example:
```
$ ./jfr-flame-graph -h
  Usage: JFRToFlameGraphWriter [options]
    Options:
      -d, --decompress
        Decompress the JFR file
        Default: false
      -et, --end-timestamp
        End timestamp in seconds for filtering
        Default: 9223372036854775807
      -e, --event
        Type of event used to generate the flamegraph
        Default: cpu
        Possible Values: [cpu, allocation-tlab, allocation-outside-tlab, exceptions, monitor-blocked, io]
      -h, --help
        Display Help
      -ha, --hide-arguments
        Hide arguments in methods
        Default: false
      -i, --ignore-line-numbers
        Ignore Line Numbers in Stack Frame
        Default: false
    * -f, --jfrdump
        Java Flight Recorder Dump
      -l, --live
        Export stack trace sample timestamp (in json output type)
        Default: false
      -o, --output
        Output file
      -ot, --output-type
        Output type
        Default: folded
        Possible Values: [folded, json]
      -j, --print-jfr-details
        Print JFR details and exit
        Default: false
      -t, --print-timestamp
        Print timestamp in JFR Details
        Default: false
      -rv, --show-return-value
        Show return value for methods in the stack
        Default: false
      -st, --start-timestamp
        Start timestamp in seconds for filtering
        Default: -9223372036854775808
      -sn, --use-simple-names
        Use simple names instead of qualified names in the stack
        Default: false
```

## License

Copyright (C) 2015 M. Isuru Tharanga Chrishantha Perera

Licensed under the Apache License, Version 2.0
