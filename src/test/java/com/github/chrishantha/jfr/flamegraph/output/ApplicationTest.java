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

import com.beust.jcommander.JCommander;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.File;
import java.io.IOException;

/**
 * Unit tests for the main Application.
 */
public class ApplicationTest extends TestCase {

    private OutputWriterParameters parameters;
    private JFRToFlameGraphWriter jfrToFlameGraphWriter;

    @Override
    protected void setUp() throws Exception {
        parameters = new OutputWriterParameters();
        jfrToFlameGraphWriter = new JFRToFlameGraphWriter(parameters);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(ApplicationTest.class);
    }

    private void parseCommands(String[] args) {
        JCommander jc = new JCommander();
        jc.addObject(jfrToFlameGraphWriter);
        jc.addObject(parameters);
        jc.parse(args);
    }

    public void testJFRToFlameGraphWriterOutputFile() throws IOException {
        File tmp = File.createTempFile(getClass().getName(), "");
        String[] args = { "-f", tmp.toString(), "-o", tmp.toString() };
        parseCommands(args);
        assertTrue(tmp.exists());
        assertEquals(tmp, jfrToFlameGraphWriter.jfrdump);
        assertEquals(tmp, jfrToFlameGraphWriter.outputFile);
        assertFalse(jfrToFlameGraphWriter.ignoreLineNumbers);
    }

    public void testIgnoreLineNumbersOption() throws IOException {
        String[] args = { "-f", "temp", "-i" };
        parseCommands(args);
        assertTrue(jfrToFlameGraphWriter.ignoreLineNumbers);
    }

    public void testLiveOption() throws IOException {
        String[] args = { "-f", "temp", "-l" };
        parseCommands(args);
        assertTrue(parameters.live);
    }

    public void testEventTypeOption() throws Exception {
        String[] args = { "-f", "temp", "-e", "allocation-tlab" };
        parseCommands(args);
        assertEquals(EventType.EVENT_ALLOCATION_IN_NEW_TLAB, jfrToFlameGraphWriter.eventType);
    }

    public void testEventTypeOptionDefaultValue() throws Exception {
        String[] args = { "-f", "temp" };
        parseCommands(args);
        assertEquals(EventType.EVENT_METHOD_PROFILING_SAMPLE, jfrToFlameGraphWriter.eventType);
    }

}
