/*
 * Copyright 2015 M. Isuru Tharanga Chrishantha Perera
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.chrishantha.jfr.flamegraph.output;

import com.beust.jcommander.Parameter;
import com.jrockit.mc.common.IMCFrame;
import com.jrockit.mc.common.IMCMethod;
import com.jrockit.mc.flightrecorder.FlightRecording;
import com.jrockit.mc.flightrecorder.FlightRecordingLoader;
import com.jrockit.mc.flightrecorder.internal.model.FLRStackTrace;
import com.jrockit.mc.flightrecorder.spi.IEvent;
import com.jrockit.mc.flightrecorder.spi.ITimeRange;
import com.jrockit.mc.flightrecorder.spi.IView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.MessageFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Stack;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

/**
 * Parse JFR dump and create a compatible output for Flame Graph
 */
public abstract class JFRToFlameGraphWriterCommand {

    @Parameter(names = {"-f", "--jfrdump"}, description = "Java Flight Recorder Dump", required = true)
    File jfrdump;

    @Parameter(names = {"-i", "--ignore-line-numbers"}, description = "Ignore Line Numbers in Stack Frame")
    boolean ignoreLineNumbers;

    @Parameter(names = {"-o", "--output"}, description = "Output file")
    File outputFile;

    @Parameter(names = {"-d", "--decompress"}, description = "Decompress the JFR file")
    boolean decompress;

    @Parameter(names = {"-r", "--show-return-value"}, description = "Show return value for methods in the stack")
    boolean showReturnValue;

    @Parameter(names = {"-s", "--use-simple-names"},
            description = "Use simple names instead of qualified names in the stack")
    boolean useSimpleNames;

    @Parameter(names = {"-a", "--hide-arguments"}, description = "Hide arguments in methods")
    boolean hideArguments;

    @Parameter(names = {"-j", "--print-jfr-details"}, description = "Print JFR details and exit")
    boolean printJFRDetails;

    @Parameter(names = {"-t", "--print-timestamp"}, description = "Print timestamp in JFR Details")
    boolean printTimestamp;

    @Parameter(names = {"-x", "--start-timestamp"}, description = "Start timestamp in seconds for filtering")
    long startTimestamp;

    @Parameter(names = {"-y", "--end-timestamp"}, description = "End timestamp in seconds for filtering")
    long endTimestamp;

    private static final String EVENT_TYPE = "Method Profiling Sample";
    private static final String EVENT_VALUE_STACK = "(stackTrace)";
    private static final String PRINT_FORMAT = "%-12s: %s%n";

    public JFRToFlameGraphWriterCommand() {
    }

    protected abstract void processEvent(long startTimestamp, long endTimestamp, long duration,
                                         Stack<String> stack);

    protected abstract void writeOutput(BufferedWriter bufferedWriter) throws IOException;

    public void process() throws IOException {
        FlightRecording recording;
        try {
            recording = FlightRecordingLoader.loadFile(decompress ? decompressFile(jfrdump) : jfrdump);
        } catch (Exception e) {
            System.err.println("Could not load the JFR file.");
            if (!decompress) {
                System.err.println("If the JFR file is compressed, try the decompress option");
            }
            e.printStackTrace();
            return;
        }

        IView view = recording.createView();

        if (printJFRDetails) {
            printJFRDetails(recording);
            return;
        }

        // Filter if start or end timestamp is passed as options
        final boolean filter = startTimestamp > 0 || endTimestamp > 0;
        startTimestamp = TimeUnit.SECONDS.toNanos(startTimestamp);
        endTimestamp = TimeUnit.SECONDS.toNanos(endTimestamp);

        long processedEvents = 0;

        for (IEvent event : view) {
            // Filter for Method Profiling Sample Events
            if (EVENT_TYPE.equals(event.getEventType().getName())) {
                long eventStartTimestamp = event.getStartTimestamp();
                long eventEndTimestamp = event.getEndTimestamp();

                if (filter && !filter(eventStartTimestamp, eventEndTimestamp)) {
                    continue;
                }

                // Get Stack Trace from the event. Field ID was identified from
                // event.getEventType().getFieldIdentifiers()
                FLRStackTrace flrStackTrace = (FLRStackTrace) event.getValue(EVENT_VALUE_STACK);
                Stack<String> stack = getStack(flrStackTrace);

                processEvent(eventStartTimestamp, eventEndTimestamp, event.getDuration(), stack);
                processedEvents++;
            }
        }

        if (processedEvents == 0) {
            System.err.println("There are no method profiling sample events");
            System.exit(1);
        }

        try (Writer writer = outputFile != null ? new FileWriter(outputFile) : new PrintWriter(System.out);
             BufferedWriter bufferedWriter = new BufferedWriter(writer);) {
            writeOutput(bufferedWriter);
        }
    }

    private boolean filter(long eventStartTimestamp, long eventEndTimestamp) {
        if (eventStartTimestamp >= startTimestamp && eventStartTimestamp <= endTimestamp) {
            return true;
        } else if (eventEndTimestamp >= startTimestamp && eventEndTimestamp <= endTimestamp) {
            return true;
        }
        return false;
    }

    private void printJFRDetails(FlightRecording recording) {
        ITimeRange timeRange = recording.getTimeRange();

        long startTimestamp = TimeUnit.NANOSECONDS.toSeconds(timeRange.getStartTimestamp());
        long endTimestamp = TimeUnit.NANOSECONDS.toSeconds(timeRange.getEndTimestamp());

        Duration d = Duration.ofNanos(timeRange.getDuration());
        long hours = d.toHours();
        long minutes = d.minusHours(hours).toMinutes();

        System.out.println("JFR Details");
        if (printTimestamp) {
            System.out.format(PRINT_FORMAT, "Start", startTimestamp);
            System.out.format(PRINT_FORMAT, "End", endTimestamp);
        } else {
            Instant startInstant = Instant.ofEpochSecond(startTimestamp);
            Instant endInstant = Instant.ofEpochSecond(endTimestamp);
            DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG)
                    .withZone(ZoneId.systemDefault());
            System.out.format(PRINT_FORMAT, "Start", formatter.format(startInstant));
            System.out.format(PRINT_FORMAT, "End", formatter.format(endInstant));
        }
        System.out.format(PRINT_FORMAT, "Duration", MessageFormat.format("{0} h {1} min", hours, minutes));
    }

    private Stack<String> getStack(FLRStackTrace flrStackTrace) {
        Stack<String> stack = new Stack<>();
        for (IMCFrame frame : flrStackTrace.getFrames()) {
            // Push method to a stack
            stack.push(getFrameName(frame));
        }
        return stack;
    }

    private String getFrameName(IMCFrame frame) {
        StringBuilder methodBuilder = new StringBuilder();
        IMCMethod method = frame.getMethod();
        methodBuilder.append(method.getHumanReadable(showReturnValue, !useSimpleNames, true, !useSimpleNames,
                !hideArguments, !useSimpleNames));
        if (!ignoreLineNumbers) {
            methodBuilder.append(":");
            methodBuilder.append(frame.getFrameLineNumber());
        }
        return methodBuilder.toString();
    }

    private File decompressFile(final File compressedFile) throws IOException {
        byte[] buffer = new byte[8 * 1024];

        File decompressedFile;

        try (GZIPInputStream compressedStream =
                     new GZIPInputStream(new FileInputStream(compressedFile));
             FileOutputStream uncompressedFileStream =
                     new FileOutputStream(decompressedFile = File.createTempFile("jfr_", null))) {

            decompressedFile.deleteOnExit();
            int numberOfBytes;

            while ((numberOfBytes = compressedStream.read(buffer)) > 0) {
                uncompressedFileStream.write(buffer, 0, numberOfBytes);
            }
        }

        return decompressedFile;
    }


}
