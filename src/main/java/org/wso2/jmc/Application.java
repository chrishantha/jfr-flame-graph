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

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

public class Application {

    public static void main(String[] args) {

        JFRToFlameGraphWriter jfrToFlameGraphWriter = new JFRToFlameGraphWriter();
        final JCommander jcmdr = new JCommander(jfrToFlameGraphWriter);
        jcmdr.setProgramName(Application.class.getSimpleName());

        try {
            jcmdr.parse(args);
            jfrToFlameGraphWriter.write();
        } catch (ParameterException e) {
            System.out.println(e.getMessage());
        }

        if (jfrToFlameGraphWriter.help) {
            jcmdr.usage();
            return;
        }
    }
}
