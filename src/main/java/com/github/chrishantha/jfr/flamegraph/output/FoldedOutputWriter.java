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

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Create folded output to be used with flamegraph.pl
 */
public class FoldedOutputWriter implements FlameGraphOutputWriter {

    /**
     * The data model for folded stacks
     */
    private final Map<String, Long> stackTraceMap = new LinkedHashMap<>();

    @Override
    public void initialize(OutputWriterParameters parameters) {
    }

    @Override
    public void processEvent(long startTimestamp, long endTimestamp, long duration, Stack<String> stack, Long value) {
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
        Long count = stackTraceMap.get(stackTrace);
        if (count == null) {
            count = value;
        } else {
            count += value;
        }
        stackTraceMap.put(stackTrace, count);
    }

    @Override
    public void writeOutput(BufferedWriter bufferedWriter) throws IOException {
        for (Map.Entry<String, Long> entry : stackTraceMap.entrySet()) {
            bufferedWriter.write(String.format("%s %d%n", entry.getKey(), entry.getValue()));
        }
    }
}
