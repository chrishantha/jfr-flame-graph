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
import com.beust.jcommander.ParameterException;

import java.io.IOException;

public class Application {

    public static void main(String[] args) throws Exception {
        final JCommander jcmdr = new JCommander();
        jcmdr.setProgramName(JFRToFlameGraphWriter.class.getSimpleName());

        OutputWriterParameters parameters = new OutputWriterParameters();
        JFRToFlameGraphWriter jfrToFlameGraphWriter = new JFRToFlameGraphWriter(parameters);

        jcmdr.addObject(parameters);
        jcmdr.addObject(jfrToFlameGraphWriter);

        try {
            jcmdr.parse(args);
        } catch (ParameterException e) {
            System.err.println(e.getMessage());
            return;
        }

        if (jfrToFlameGraphWriter.help) {
            jcmdr.usage();
            return;
        }

        try {
            jfrToFlameGraphWriter.process();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            throw e;
        }
    }
}
