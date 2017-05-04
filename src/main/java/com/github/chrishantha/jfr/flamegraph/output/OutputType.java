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

/**
 * Output Types for different FlameGraph implementations
 */
public enum OutputType {

    /**
     * Create folded output
     */
    FOLDED("folded", new FoldedOutputWriter()),

    /**
     * Create json output for d3-flame-graph
     */
    JSON("json", new JsonOutputWriter());

    private final String name;
    private final FlameGraphOutputWriter flameGraphOutputWriter;

    OutputType(String name, FlameGraphOutputWriter flameGraphOutputWriter) {
        this.name = name;
        this.flameGraphOutputWriter = flameGraphOutputWriter;
    }

    public FlameGraphOutputWriter getFlameGraphOutputWriter() {
        return flameGraphOutputWriter;
    }

    @Override
    public String toString() {
        return name;
    }
}
