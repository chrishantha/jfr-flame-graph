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

import java.io.File;
import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.beust.jcommander.JCommander;

/**
 * Unit tests for the main Application.
 */
public class ApplicationTest extends TestCase {

    private JFRToFlameGraphWriter jfrToFlameGraphWriter;

    @Override
    protected void setUp() throws Exception {
        jfrToFlameGraphWriter = new JFRToFlameGraphWriter();
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

    public void testOptions() throws IOException {
        File tmp = File.createTempFile(getClass().getName(), "");
        String[] args = { "-f", tmp.toString(), "-o", tmp.toString() };
        JCommander jc = new JCommander();
        jc.addObject(jfrToFlameGraphWriter);
        jc.parse(args);
        assertTrue(tmp.exists());
        assertEquals(tmp, jfrToFlameGraphWriter.jfrdump);
        assertEquals(tmp, jfrToFlameGraphWriter.outputFile);
        assertFalse(jfrToFlameGraphWriter.ignoreLineNumbers);
    }

    public void testIgnoreLineNumbersOption() throws IOException {
        String[] args = { "-f", "temp", "-i" };
        JCommander jc = new JCommander();
        jc.addObject(jfrToFlameGraphWriter);
        jc.parse(args);
        assertTrue(jfrToFlameGraphWriter.ignoreLineNumbers);
    }
}
