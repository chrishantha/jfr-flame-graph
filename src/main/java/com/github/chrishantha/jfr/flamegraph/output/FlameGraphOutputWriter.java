/*
 * Copyright 2017 M. Isuru Tharanga Chrishantha Perera
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
import java.util.Stack;

/**
 * Process stack traces and write the output
 */
public interface FlameGraphOutputWriter {

    void initialize(OutputWriterParameters parameters);

    void processEvent(long startTimestamp, long endTimestamp, long duration, Stack<String> stack, Long value);

    void writeOutput(BufferedWriter bufferedWriter) throws IOException;
}
