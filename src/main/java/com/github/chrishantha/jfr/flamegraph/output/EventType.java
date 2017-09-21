package com.github.chrishantha.jfr.flamegraph.output;

import com.beust.jcommander.IStringConverter;

/**
 * Different types of events possibly available in a JFR recording.
 */
public enum EventType {

    EVENT_METHOD_PROFILING_SAMPLE("Method Profiling Sample", "cpu"),
    EVENT_ALLOCATION_IN_NEW_TLAB("Allocation in new TLAB", "allocation-tlab", true), 
    EVENT_ALLOCATION_OUTSIDE_TLAB("Allocation outside TLAB", "allocation-outside-tlab", true), 
    EVENT_JAVA_EXCEPTION("Java Exception", "exceptions"),
    EVENT_JAVA_MONITOR_BLOCKED("Java Monitor Blocked", "monitor-blocked");

    /** Name as declared in the JFR recording */
    private final String name;

    /** Id used as a command line option */
    private final String id;

    /** True if the event is allocation-related */
    private final boolean isAllocation;

    EventType(String name, String id, boolean isAllocation) {
        this.name = name;
        this.id = id;
        this.isAllocation = isAllocation;
    }

    EventType(String name, String id) {
        this(name, id, false);
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return id;
    }

    /**
     * @return true if the event is allocated related
     */
    public boolean isAllocation() {
        return isAllocation;
    }

    public static EventType from(String name) {
        switch (name) {
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
            throw new IllegalArgumentException("Event type [" + name + "] does not exist.");
        }
    }

    public static final class EventTypeConverter implements IStringConverter<EventType> {
        @Override
        public EventType convert(String value) {
            return EventType.from(value);
        }
    }
}
