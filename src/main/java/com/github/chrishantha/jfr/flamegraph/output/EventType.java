/*
 * Copyright 2017 Leonardo Freitas Gomes
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

import com.beust.jcommander.IStringConverter;
import com.jrockit.mc.flightrecorder.spi.IEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Different types of events possibly available in a JFR recording.
 * <p>
 * Each type can be activated using a command line option and can match one or many
 * JFR event types. Each type knows how to convert the event into a numeric value
 * that will make the flame graph most meaningful. For allocation events this would
 * be the number of bytes allocated, while for file reads it would be the duration of
 * the read operation.
 */
public enum EventType {

    METHOD_PROFILING_SAMPLE("cpu", ValueField.COUNT, "Method Profiling Sample"),
    ALLOCATION_IN_NEW_TLAB("allocation-tlab", ValueField.TLAB_SIZE, "Allocation in new TLAB"),
    ALLOCATION_OUTSIDE_TLAB("allocation-outside-tlab", ValueField.ALLOCATION_SIZE, "Allocation outside TLAB"),
    JAVA_EXCEPTION("exceptions", ValueField.COUNT, "Java Exception"),
    JAVA_MONITOR_BLOCKED("monitor-blocked", ValueField.DURATION, "Java Monitor Blocked"),
    IO("io", ValueField.DURATION, "File Read", "File Write", "Socket Read", "Socket Write");

    private final String commandLineOption;
    private final ValueField valueField;
    private final String[] eventNames;

    EventType(String commandLineOption, ValueField valueField, String... eventNames) {
        this.eventNames = eventNames;
        this.commandLineOption = commandLineOption;
        this.valueField = valueField;
    }

    public boolean matches(IEvent event) {
        String name = event.getEventType().getName();
        return Arrays.stream(eventNames).anyMatch(name::equals);
    }

    public long getValue(IEvent event) {
        return valueField.getValue(event);
    }

    @Override
    public String toString() {
        return commandLineOption;
    }


    public static final class EventTypeConverter implements IStringConverter<EventType> {
        private static final Map<String, EventType> typesByOption = new HashMap<>();

        static {
            for (EventType type : EventType.values()) {
                typesByOption.put(type.commandLineOption, type);
            }
        }

        @Override
        public EventType convert(String commandLineOption) {
            EventType eventType = typesByOption.get(commandLineOption);
            if (eventType == null) {
                throw new IllegalArgumentException("Event type [" + commandLineOption + "] does not exist.");
            }
            return eventType;
        }
    }

    private enum ValueField {
        COUNT {
            @Override
            public long getValue(IEvent event) {
                return 1;
            }
        },
        DURATION {
            @Override
            public long getValue(IEvent event) {
                long nanos = (long) event.getValue("(duration)");
                return TimeUnit.NANOSECONDS.toMillis(nanos);
            }
        },
        ALLOCATION_SIZE {
            @Override
            public long getValue(IEvent event) {
                return (long) event.getValue("allocationSize") / 1000;
            }
        },
        TLAB_SIZE {
            @Override
            public long getValue(IEvent event) {
                return (long) event.getValue("tlabSize") / 1000;
            }
        };

        public abstract long getValue(IEvent event);
    }
}
