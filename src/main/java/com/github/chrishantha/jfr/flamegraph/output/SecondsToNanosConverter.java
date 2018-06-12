/*
 * Copyright 2018 Stefan Oehme
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

import com.beust.jcommander.converters.LongConverter;

import java.util.concurrent.TimeUnit;

public class SecondsToNanosConverter extends LongConverter {
    public SecondsToNanosConverter(String optionName) {
        super(optionName);
    }

    @Override
    public Long convert(String value) {
        Long seconds = super.convert(value);
        return TimeUnit.SECONDS.toNanos(seconds);
    }
}
