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

Run `mvn clean install`.

## How to run

After building, you can just run the JAR file. There is a helper script named `run.sh` to run the jar file.

For example:

```
./run.sh -f /tmp/highcpu.jfr -o /tmp/output.txt
```

Following are the options available.
```
$ ./run.sh -h
The following option is required: -f, --jfrdump 
Usage: Application [options]
  Options:
    -h, --help
       Display Help
       Default: false
    -i, --ignore-line-numbers
       Ignore Line Numbers in Stack Frame
       Default: false
  * -f, --jfrdump
       Java Flight Recorder Dump
    -o, --output
       Output file
```

## How to generate a Flame Graph

Clone [Brendan]'s [FlameGraph] repository, generate FlameGraph using `flamegraph.pl` and open the SVG file in your web browser.

```
git clone https://github.com/brendangregg/FlameGraph.git
cd FlameGraph
cat /tmp/output.txt | ./flamegraph.pl > ../traces-highcpu.svg
firefox ../traces-highcpu.svg
```

[Brendan]: http://www.brendangregg.com/bio.html

## License

Copyright (C) 2015 M. Isuru Tharanga Chrishantha Perera

Licensed under the Apache License, Version 2.0
