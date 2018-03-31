package com.github.chrishantha.jfr.flamegraph.output;

import com.beust.jcommander.IStringConverter;
import com.jrockit.mc.flightrecorder.spi.IEvent;

/**
 * Different types of events possibly available in a JFR recording.
 *
 * Each type can be activated using a command line option and can match one or many
 * JFR event types. Each type knows how to convert the event into a numeric value
 * that will make the flame graph most meaningful. For allocation events this would
 * be the number of bytes allocated, while for file reads it would be the duration of
 * the read operation.
 */
public enum EventType {

    EVENT_METHOD_PROFILING_SAMPLE("Method Profiling Sample", "cpu"),
    EVENT_ALLOCATION_IN_NEW_TLAB("Allocation in new TLAB", "allocation-tlab", "tlabSize"),
    EVENT_ALLOCATION_OUTSIDE_TLAB("Allocation outside TLAB", "allocation-outside-tlab", "allocationSize"),
    EVENT_JAVA_EXCEPTION("Java Exception", "exceptions"),
    EVENT_JAVA_MONITOR_BLOCKED("Java Monitor Blocked", "monitor-blocked");

    private final String name;
    private final String commandLineOption;
    private final String valueField;

    EventType(String name, String commandLineOption) {
        this(name, commandLineOption, null);
    }

    EventType(String name, String commandLineOption, String valueField) {
        this.name = name;
        this.commandLineOption = commandLineOption;
        this.valueField = valueField;
    }

    public boolean matches(IEvent event) {
        return name.equals(event.getEventType().getName());
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
                    return EVENT_ALLOCATION_IN_NEW_TLAB;
                case "allocation-outside-tlab":
                    return EVENT_ALLOCATION_OUTSIDE_TLAB;
                case "exceptions":
                    return EVENT_JAVA_EXCEPTION;
                case "monitor-blocked":
                    return EVENT_JAVA_MONITOR_BLOCKED;
                case "cpu":
                    return EVENT_METHOD_PROFILING_SAMPLE;
                default:
                    throw new IllegalArgumentException("Event type [" + commandLineOption + "] does not exist.");
            }
        }
    }
}
