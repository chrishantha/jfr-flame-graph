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
import jdk.jfr.consumer.*;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

/**
 * Parse JFR dump and create a compatible output for Flame Graph
 */
public final class JFRToFlameGraphWriter {

    private final OutputWriterParameters parameters;
    @Parameter(names = {"-h", "--help"}, description = "Display Help", help = true)
    boolean help;

    @Parameter(names = {"-f", "--jfrdump"}, description = "Java Flight Recorder Dump", required = true)
    File jfrdump;

    @Parameter(names = {"-ot", "--output-type"}, description = "Output type")
    OutputType outputType = OutputType.FOLDED;

    @Parameter(names = {"-o", "--output"}, description = "Output file")
    File outputFile;

    @Parameter(names = {"-d", "--decompress"}, description = "Decompress the JFR file")
    boolean decompress;

    @Parameter(names = {"-i", "--ignore-line-numbers"}, description = "Ignore Line Numbers in Stack Frame")
    boolean ignoreLineNumbers;

    @Parameter(names = {"-rv", "--show-return-value"}, description = "Show return value for methods in the stack")
    boolean showReturnValue;

    @Parameter(names = {"-sn",
            "--use-simple-names"}, description = "Use simple names instead of qualified names in the stack")
    boolean useSimpleNames;

    @Parameter(names = {"-ha", "--hide-arguments"}, description = "Hide arguments in methods")
    boolean hideArguments;

    @Parameter(names = {"-j", "--print-jfr-details"}, description = "Print JFR details and exit")
    boolean printJFRDetails;

    @Parameter(names = {"-t", "--print-timestamp"}, description = "Print timestamp in JFR Details")
    boolean printTimestamp;

    @Parameter(names = {"-st", "--start-timestamp"}, description = "Start timestamp in seconds for filtering", converter = SecondsToNanosConverter.class)
    long startTimestamp = Long.MIN_VALUE;

    @Parameter(names = {"-et", "--end-timestamp"}, description = "End timestamp in seconds for filtering", converter = SecondsToNanosConverter.class)
    long endTimestamp = Long.MAX_VALUE;

    @Parameter(names = {"-e",
            "--event"}, description = "Type of event used to generate the flamegraph", converter = EventType.EventTypeConverter.class)
    EventType eventType = EventType.METHOD_PROFILING_SAMPLE;

    private static final String EVENT_VALUE_STACK = "stackTrace";

    private static final String PRINT_FORMAT = "%-16s: %s%n";

    private static final String DURATION_FORMAT = "{0} h {1} min";

    public JFRToFlameGraphWriter(OutputWriterParameters parameters) {
        this.parameters = parameters;
    }

    public void process() throws Exception {
        RecordingFile recording = loadRecording();

        if (printJFRDetails) {
            printJFRDetails(recording);
        } else {
            convertToStacks(recording);
        }
    }

    private RecordingFile loadRecording() throws IOException {
        RecordingFile recording;
        try {
            recording = new RecordingFile(jfrdump.toPath());
        } catch (Exception e) {
            System.err.println("Could not load the JFR file.");
            if (!decompress) {
                System.err.println("If the JFR file is compressed, try the decompress option");
            }
            throw e;
        }
        return recording;
    }

    private void convertToStacks(RecordingFile recording) throws IOException {

//        System.out.println(recording.readEventTypes().stream().map(e -> e.getName()).collect(Collectors.toList()));

        FlameGraphOutputWriter flameGraphOutputWriter = outputType.createFlameGraphOutputWriter();
        flameGraphOutputWriter.initialize(parameters);

        while (recording.hasMoreEvents()) {
            RecordedEvent event = recording.readEvent();
            if (!eventType.matches(event)) {
                continue;
            }
            if (!matchesTimeRange(event)) {
                continue;
            }

            RecordedStackTrace flrStackTrace = (RecordedStackTrace) event.getValue(EVENT_VALUE_STACK);
            if (flrStackTrace != null) {
                Stack<String> stack = getStack(event);
                long value = eventType.getValue(event);
                flameGraphOutputWriter.processEvent(event.getStartTime(), event.getEndTime(), event.getDuration(), stack, value);
            }
        }

        try (Writer writer = outputFile != null ? new FileWriter(outputFile) : new PrintWriter(System.out);
             BufferedWriter bufferedWriter = new BufferedWriter(writer)) {
            flameGraphOutputWriter.writeOutput(bufferedWriter);
        }
    }

    private boolean matchesTimeRange(RecordedEvent event) {
        Instant eventStartTimestamp = event.getStartTime();
        Instant eventEndTimestamp = event.getEndTime();
        if (eventStartTimestamp.getNano() >= startTimestamp && eventStartTimestamp.getNano() <= endTimestamp) {
            return true;
        } else if (eventEndTimestamp.getNano() >= startTimestamp && eventEndTimestamp.getNano() <= endTimestamp) {
            return true;
        }
        return false;
    }

    private void printJFRDetails(RecordingFile recording) throws IOException {
//        ITimeRange timeRange = recording.getTimeRange();

//        long startTimestamp = TimeUnit.NANOSECONDS.toSeconds(timeRange.getStartTimestamp());
//        long endTimestamp = TimeUnit.NANOSECONDS.toSeconds(timeRange.getEndTimestamp());
//
//        Duration d = Duration.ofNanos(timeRange.getDuration());
//        long hours = d.toHours();
//        long minutes = d.minusHours(hours).toMinutes();

//        IView view = recording.createView();

        Instant minEventStartTimestamp = Instant.MAX;
        Instant maxEventEndTimestamp = Instant.MIN;

//        view.setFilter(eventType::matches);

        while (recording.hasMoreEvents()) {
            RecordedEvent event = recording.readEvent();
            Instant eventStartTimestamp = event.getStartTime();
            Instant eventEndTimestamp = event.getEndTime();
            if (eventStartTimestamp.isBefore(minEventStartTimestamp)) {
                minEventStartTimestamp = eventStartTimestamp;
            }

            if (eventEndTimestamp.isAfter(maxEventEndTimestamp)) {
                maxEventEndTimestamp = eventEndTimestamp;
            }
        }

        Duration eventsDuration = Duration.between(minEventStartTimestamp, maxEventEndTimestamp);
        long eventHours = eventsDuration.toHours();
        long eventMinutes = eventsDuration.minusHours(eventHours).toMinutes();

        System.out.println("JFR Details");
        if (printTimestamp) {
//            System.out.format(PRINT_FORMAT, "Start", startTimestamp);
//            System.out.format(PRINT_FORMAT, "End", endTimestamp);
            System.out.format(PRINT_FORMAT, "Min Start Event", minEventStartTimestamp);
            System.out.format(PRINT_FORMAT, "Max End Event", maxEventEndTimestamp);
        } else {
//            Instant startInstant = Instant.ofEpochSecond(startTimestamp);
//            Instant endInstant = Instant.ofEpochSecond(endTimestamp);
            DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG)
                    .withZone(ZoneId.systemDefault());
//            System.out.format(PRINT_FORMAT, "Start", formatter.format(startInstant));
//            System.out.format(PRINT_FORMAT, "End", formatter.format(endInstant));
            System.out.format(PRINT_FORMAT, "Min Start Event", formatter.format(minEventStartTimestamp));
            System.out.format(PRINT_FORMAT, "Max End Event", formatter.format(maxEventEndTimestamp));
        }
//        System.out.format(PRINT_FORMAT, "JFR Duration", MessageFormat.format(DURATION_FORMAT, hours, minutes));
        System.out.format(PRINT_FORMAT, "Events Duration",
                MessageFormat.format(DURATION_FORMAT, eventHours, eventMinutes));
    }

    private Stack<String> getStack(RecordedEvent event) {
        RecordedStackTrace flrStackTrace = (RecordedStackTrace) event.getValue(EVENT_VALUE_STACK);
        Stack<String> stack = new Stack<>();
        if (flrStackTrace == null) {
            return stack;
        }
        for (RecordedFrame frame : flrStackTrace.getFrames()) {
            String frameName = getFrameName(frame);
            if (frameName != null) {
                stack.push(frameName);
            }
        }
        return stack;
    }

    private String getFrameName(RecordedFrame frame) {
        StringBuilder methodBuilder = new StringBuilder();
        RecordedMethod method = frame.getMethod();
        if (method == null) {
            return null;
        }

//        methodBuilder.append(method.getHumanReadable(showReturnValue, !useSimpleNames, true, !useSimpleNames, !hideArguments, !useSimpleNames));
        methodBuilder.append(formatMethod(method));
        if (!ignoreLineNumbers) {
            methodBuilder.append(":");
            methodBuilder.append(frame.getLineNumber());
        }
        return methodBuilder.toString();
    }

    private File decompressFile(final File compressedFile) throws IOException {
        byte[] buffer = new byte[8 * 1024];

        File decompressedFile;

        try (GZIPInputStream compressedStream = new GZIPInputStream(new FileInputStream(compressedFile));
             FileOutputStream uncompressedFileStream = new FileOutputStream(
                     decompressedFile = File.createTempFile("jfr_", null))) {

            decompressedFile.deleteOnExit();
            int numberOfBytes;

            while ((numberOfBytes = compressedStream.read(buffer)) > 0) {
                uncompressedFileStream.write(buffer, 0, numberOfBytes);
            }
        }

        return decompressedFile;
    }

    private String formatMethod(RecordedMethod m) {
        StringBuilder sb = new StringBuilder();
        sb.append(m.getType().getName());
        sb.append(".");
        sb.append(m.getName());
        sb.append("(");
        StringJoiner sj = new StringJoiner(", ");
        String md = m.getDescriptor().replace("/", ".");
        String parameter = md.substring(1, md.lastIndexOf(")"));
        for (String qualifiedName : decodeDescriptors(parameter, "")) {
            String typeName = qualifiedName.substring(qualifiedName.lastIndexOf('.') + 1);
            sj.add(typeName);
        }
        sb.append(sj);
        sb.append(")");
        return sb.toString();
    }

    List<String> decodeDescriptors(String descriptor, String arraySize) {
        List<String> descriptors = new ArrayList<>();
        for (int index = 0; index < descriptor.length(); index++) {
            String arrayBrackets = "";
            while (descriptor.charAt(index) == '[') {
                arrayBrackets = arrayBrackets +  "[" + arraySize + "]" ;
                arraySize = "";
                index++;
            }
            char c = descriptor.charAt(index);
            String type;
            switch (c) {
                case 'L':
                    int endIndex = descriptor.indexOf(';', index);
                    type = descriptor.substring(index + 1, endIndex);
                    index = endIndex;
                    break;
                case 'I':
                    type = "int";
                    break;
                case 'J':
                    type = "long";
                    break;
                case 'Z':
                    type = "boolean";
                    break;
                case 'D':
                    type = "double";
                    break;
                case 'F':
                    type = "float";
                    break;
                case 'S':
                    type = "short";
                    break;
                case 'C':
                    type = "char";
                    break;
                case 'B':
                    type = "byte";
                    break;
                default:
                    type = "<unknown-descriptor-type>";
            }
            descriptors.add(type + arrayBrackets);
        }
        return descriptors;
    }

}
