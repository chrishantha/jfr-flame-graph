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
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

import java.io.IOException;

public class Application {

    @Parameter(names = {"-h", "--help"}, description = "Display Help")
    private boolean help = false;

    private static final String FOLDED_OUTPUT_COMMAND = "folded";

    private static final String JSON_OUTPUT_COMMAND = "json";

    public static void main(String[] args) {
        Application application = new Application();
        final JCommander jcmdr = new JCommander(application);
        jcmdr.setProgramName(Application.class.getSimpleName());

        // Add commands
        FoldedOutputCommand foldedOutputCommand = new FoldedOutputCommand();
        jcmdr.addCommand(FOLDED_OUTPUT_COMMAND, foldedOutputCommand);
        JsonOutputCommand jsonOutputCommand = new JsonOutputCommand();
        jcmdr.addCommand(JSON_OUTPUT_COMMAND, jsonOutputCommand);

        try {
            jcmdr.parse(args);
        } catch (ParameterException e) {
            System.err.println(e.getMessage());
            return;
        }

        if (application.help) {
            jcmdr.usage();
            return;
        }

        try {
            String command = jcmdr.getParsedCommand();
            if (FOLDED_OUTPUT_COMMAND.equals(command)) {
                foldedOutputCommand.process();
            } else if (JSON_OUTPUT_COMMAND.equals(command)) {
                jsonOutputCommand.process();
            } else {
                jcmdr.usage();
                return;
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
