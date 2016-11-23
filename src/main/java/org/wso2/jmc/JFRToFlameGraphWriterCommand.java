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
package org.wso2.jmc;

import com.beust.jcommander.Parameter;
import com.jrockit.mc.common.IMCFrame;
import com.jrockit.mc.common.IMCMethod;
import com.jrockit.mc.flightrecorder.FlightRecording;
import com.jrockit.mc.flightrecorder.FlightRecordingLoader;
import com.jrockit.mc.flightrecorder.internal.model.FLRStackTrace;
import com.jrockit.mc.flightrecorder.spi.IEvent;
import com.jrockit.mc.flightrecorder.spi.IView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Stack;
import java.util.zip.GZIPInputStream;

/**
 * Parse JFR dump and create a compatible output for Flame Graph
 */
public abstract class JFRToFlameGraphWriterCommand {

    @Parameter(names = {"-f", "--jfrdump"}, description = "Java Flight Recorder Dump", required = true)
    File jfrdump;

    @Parameter(names = {"-i", "--ignore-line-numbers"}, description = "Ignore Line Numbers in Stack Frame")
    boolean ignoreLineNumbers = false;

    @Parameter(names = {"-o", "--output"}, description = "Output file")
    File outputFile;

    @Parameter(names = {"-d", "--decompress"}, description = "Decompress the JFR file")
    boolean decompress = false;

    private final String EVENT_TYPE = "Method Profiling Sample";
    private final String EVENT_VALUE_STACK = "(stackTrace)";

    public JFRToFlameGraphWriterCommand() {
    }

    protected abstract void processEvent(long startTimestamp, long endTimestamp, long duration,
                                         Stack<String> stack);

    protected abstract String getDefaultOutputFile();

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

        for (IEvent event : view) {
            // Filter for Method Profiling Sample Events
            if (EVENT_TYPE.equals(event.getEventType().getName())) {
                // Get Stack Trace from the event. Field ID was identified from
                // event.getEventType().getFieldIdentifiers()
                FLRStackTrace flrStackTrace = (FLRStackTrace) event.getValue(EVENT_VALUE_STACK);
                Stack<String> stack = getStack(flrStackTrace);

                processEvent(event.getStartTimestamp(), event.getEndTimestamp(), event.getDuration(), stack);
            }
        }

        if (outputFile == null) {
            outputFile = new File(getDefaultOutputFile());
        }

        try (FileWriter fileWriter = new FileWriter(outputFile);
             BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);) {
            writeOutput(bufferedWriter);
        }
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
        methodBuilder.append(method.getHumanReadable(false, true, true, true, true, true));
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
