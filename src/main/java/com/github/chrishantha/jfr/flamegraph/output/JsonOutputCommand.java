/*
 * Copyright 2016 M. Isuru Tharanga Chrishantha Perera
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
import com.beust.jcommander.Parameters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Create JSON output to be used with d3-flame-graph. This is similar to https://github.com/spiermar/node-stack-convert
 */
@Parameters(commandNames = "json", commandDescription = "Create json output for d3-flame-graph")
public class JsonOutputCommand extends JFRToFlameGraphWriterCommand {

    @Parameter(names = {"-l", "--live"}, description = "Export stack trace sample timestamp")
    boolean exportTimestamp = false;

    /**
     * The data model for live json
     */
    private LiveRecording liveRecording = new LiveRecording();

    /**
     * The data model for json
     */
    private StackFrame profile = new StackFrame("root");

    private class LiveRecording {
        Map<Long, StackFrame> profilesMap = new HashMap<>();

        public StackFrame getProfile(long startTimestampSecEpoch) {
            StackFrame profile = profilesMap.get(startTimestampSecEpoch);
            if (profile == null) {
                profile = new StackFrame("root");
                profilesMap.put(startTimestampSecEpoch, profile);
            }
            return profile;
        }
    }

    private class StackFrame {

        String name;
        int value = 0;
        List<StackFrame> children = null;
        transient Map<String, StackFrame> childrenMap = new HashMap<>();

        public StackFrame(String name) {
            this.name = name;
        }

        public StackFrame addFrame(String frameName) {
            if (children == null) {
                children = new ArrayList<>();
            }
            StackFrame frame = childrenMap.get(frameName);
            if (frame == null) {
                frame = new StackFrame(frameName);
                childrenMap.put(frameName, frame);
                children.add(frame);
            }
            frame.value++;
            return frame;
        }
    }

    @Override
    protected void processEvent(long startTimestamp, long endTimestamp, long duration, Stack<String> stack) {
        StackFrame frame;
        if (exportTimestamp) {
            long startTimestampSecEpoch = startTimestamp / 1_000_000_000L;
            frame = liveRecording.getProfile(startTimestampSecEpoch);
        } else {
            frame = profile;
        }

        while (!stack.empty()) {
            frame = frame.addFrame(stack.pop());
        }
    }

    @Override
    protected String getDefaultOutputFile() {
        return "output.json";
    }

    @Override
    protected void writeOutput(BufferedWriter bufferedWriter) throws IOException {
        Gson gson = new GsonBuilder().create();
        if (exportTimestamp) {
            gson.toJson(this.liveRecording.profilesMap, bufferedWriter);
        } else {
            gson.toJson(this.profile, bufferedWriter);
        }
    }
}
