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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.zip.GZIPInputStream;

import com.beust.jcommander.Parameter;
import com.jrockit.mc.common.IMCFrame;
import com.jrockit.mc.common.IMCMethod;
import com.jrockit.mc.flightrecorder.FlightRecording;
import com.jrockit.mc.flightrecorder.FlightRecordingLoader;
import com.jrockit.mc.flightrecorder.internal.model.FLRStackTrace;
import com.jrockit.mc.flightrecorder.spi.IEvent;
import com.jrockit.mc.flightrecorder.spi.IView;

/**
 * Parse JFR dump and create a compatible output for Flame Graph
 */
public class JFRToFlameGraphWriter {

    @Parameter(names = { "-f", "--jfrdump" }, description = "Java Flight Recorder Dump", required = true)
    File jfrdump;

    @Parameter(names = { "-i", "--ignore-line-numbers" }, description = "Ignore Line Numbers in Stack Frame")
    boolean ignoreLineNumbers = false;

    @Parameter(names = { "-o", "--output" }, description = "Output file")
    File outputFile;

    @Parameter(names = { "-h", "--help" }, description = "Display Help")
    boolean help = false;


    public JFRToFlameGraphWriter() {
    }

    public void write() throws IOException {
        FlightRecording recording = null;

        try {
            recording = FlightRecordingLoader.loadFile(jfrdump);
        }
        catch( Exception e ) {
            recording = FlightRecordingLoader.loadFile(decompressFile( jfrdump ));
        }

        final String EVENT_TYPE = "Method Profiling Sample";
        Map<String, Integer> stackTraceMap = new LinkedHashMap<String, Integer>();
        IView view = recording.createView();
        for (IEvent event : view) {
            // Filter for Method Profiling Sample Events
            if (EVENT_TYPE.equals(event.getEventType().getName())) {
                // Get Stack Trace from the event. Field ID was identified from
                // event.getEventType().getFieldIdentifiers()
                FLRStackTrace flrStackTrace = (FLRStackTrace) event.getValue("(stackTrace)");

                Stack<String> stack = new Stack<String>();
                for (IMCFrame frame : flrStackTrace.getFrames()) {
                    StringBuilder methodBuilder = new StringBuilder();
                    IMCMethod method = frame.getMethod();
                    methodBuilder.append(method.getHumanReadable(false, true, true, true, true, true));
                    if (!ignoreLineNumbers) {
                        methodBuilder.append(":");
                        methodBuilder.append(frame.getFrameLineNumber());
                    }
                    // Push method to a stack
                    stack.push(methodBuilder.toString());
                }

                // StringBuilder to keep stack trace
                StringBuilder stackTraceBuilder = new StringBuilder();
                boolean appendSemicolon = false;
                while (!stack.empty()) {
                    if (appendSemicolon) {
                        stackTraceBuilder.append(";");
                    } else {
                        appendSemicolon = true;
                    }
                    stackTraceBuilder.append(stack.pop());
                }
                String stackTrace = stackTraceBuilder.toString();
                Integer count = stackTraceMap.get(stackTrace);
                if (count == null) {
                    count = 1;
                } else {
                    count++;
                }
                stackTraceMap.put(stackTrace, count);
            }
        }

        FileWriter fileWriter;
        if (outputFile == null) {
            outputFile = new File("output.txt");
        }
        fileWriter = new FileWriter(outputFile);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        for (Entry<String, Integer> entry : stackTraceMap.entrySet()) {
            bufferedWriter.write(String.format("%s %d%n", entry.getKey(), entry.getValue()));
        }
        bufferedWriter.close();
    }

    private File decompressFile( final File compressedFile ) throws IOException
    {
        byte[] buffer = new byte[1024];

        GZIPInputStream  compressedStream = null;
        FileOutputStream uncompressedFileStream = null;
        File             decompressedFile = null;


        try {
            compressedStream = new GZIPInputStream(new FileInputStream(compressedFile));

            decompressedFile = File.createTempFile("flightrecorder_", null);
            decompressedFile.deleteOnExit();
            uncompressedFileStream = new FileOutputStream(decompressedFile);

            int numberOfBytes;

            while ((numberOfBytes = compressedStream.read(buffer)) > 0) {
                uncompressedFileStream.write(buffer, 0, numberOfBytes);
            }
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
        finally {
            compressedStream.close();
            uncompressedFileStream.close();
        }

        return decompressedFile;
    }


}
