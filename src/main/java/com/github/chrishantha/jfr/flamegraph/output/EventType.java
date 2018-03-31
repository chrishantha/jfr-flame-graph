package com.github.chrishantha.jfr.flamegraph.output;

import com.beust.jcommander.IStringConverter;
import com.jrockit.mc.flightrecorder.spi.IEvent;

import java.util.Arrays;

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

    METHOD_PROFILING_SAMPLE("cpu", null, "Method Profiling Sample"),
    ALLOCATION_IN_NEW_TLAB("allocation-tlab", "tlabSize", "Allocation in new TLAB"),
    ALLOCATION_OUTSIDE_TLAB("allocation-outside-tlab", "allocationSize", "Allocation outside TLAB"),
    JAVA_EXCEPTION("exceptions", null, "Java Exception"),
    JAVA_MONITOR_BLOCKED("monitor-blocked", "(duration)", "Java Monitor Blocked"),
    IO("io", "(duration)", "File Read", "File Write", "Socket Read", "Socket Write");

    private final String commandLineOption;
    private final String valueField;
    private final String[] eventNames;

    EventType(String commandLineOption, String valueField, String... eventNames) {
        this.eventNames = eventNames;
        this.commandLineOption = commandLineOption;
        this.valueField = valueField;
    }

    public boolean matches(IEvent event) {
        String name = event.getEventType().getName();
        return Arrays.stream(eventNames).anyMatch(name::equals);
    }

    public long getValue(IEvent event) {
        if (valueField == null) {
            return 1;
        }
        return (long) event.getValue(valueField);
    }

    @Override
    public String toString() {
        return commandLineOption;
    }


    public static final class EventTypeConverter implements IStringConverter<EventType> {
        @Override
        public EventType convert(String commandLineOption) {
            switch (commandLineOption) {
                case "allocation-tlab":
                    return ALLOCATION_IN_NEW_TLAB;
                case "allocation-outside-tlab":
                    return ALLOCATION_OUTSIDE_TLAB;
                case "exceptions":
                    return JAVA_EXCEPTION;
                case "monitor-blocked":
                    return JAVA_MONITOR_BLOCKED;
                case "cpu":
                    return METHOD_PROFILING_SAMPLE;
                case "io":
                    return IO;
                default:
                    throw new IllegalArgumentException("Event type [" + commandLineOption + "] does not exist.");
            }
        }
    }
}
