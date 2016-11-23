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

    private Application application;

    private FoldedOutputCommand foldedOutputCommand;

    private JsonOutputCommand jsonOutputCommand;

    private static final String FOLDED_OUTPUT_COMMAND = "folded";

    private static final String JSON_OUTPUT_COMMAND = "json";

    @Override
    protected void setUp() throws Exception {
        application = new Application();
        foldedOutputCommand = new FoldedOutputCommand();
        jsonOutputCommand = new JsonOutputCommand();
    }

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public ApplicationTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(ApplicationTest.class);
    }

    private void parseCommands(JFRToFlameGraphWriterCommand jfrToFlameGraphWriterCommand,
                               String commandName, String[] args) {
        JCommander jc = new JCommander();
        jc.addObject(application);
        jc.addCommand(commandName, jfrToFlameGraphWriterCommand);
        jc.parse(args);
        assertEquals(commandName, jc.getParsedCommand());
    }

    private void testJFRToFlameGraphWriterCommand(JFRToFlameGraphWriterCommand jfrToFlameGraphWriterCommand,
                                                  String commandName) throws IOException {
        File tmp = File.createTempFile(getClass().getName(), "");
        String[] args = {commandName, "-f", tmp.toString(), "-o", tmp.toString()};
        parseCommands(jfrToFlameGraphWriterCommand, commandName, args);
        assertTrue(tmp.exists());
        assertEquals(tmp, jfrToFlameGraphWriterCommand.jfrdump);
        assertEquals(tmp, jfrToFlameGraphWriterCommand.outputFile);
        assertFalse(jfrToFlameGraphWriterCommand.ignoreLineNumbers);
    }

    public void testFoldedOutputCommandOptions() throws IOException {
        testJFRToFlameGraphWriterCommand(foldedOutputCommand, FOLDED_OUTPUT_COMMAND);
    }

    public void testJsonOutputCommandOptions() throws IOException {
        testJFRToFlameGraphWriterCommand(jsonOutputCommand, JSON_OUTPUT_COMMAND);
    }

    public void testIgnoreLineNumbersOption() throws IOException {
        String[] args = {FOLDED_OUTPUT_COMMAND, "-f", "temp", "-i"};
        parseCommands(foldedOutputCommand, FOLDED_OUTPUT_COMMAND, args);
        assertTrue(foldedOutputCommand.ignoreLineNumbers);
    }

    public void testLiveOption() throws IOException {
        String[] args = {JSON_OUTPUT_COMMAND, "-f", "temp", "-l"};
        parseCommands(jsonOutputCommand, JSON_OUTPUT_COMMAND, args);
        assertTrue(jsonOutputCommand.exportTimestamp);
    }

}
