package com.github.chrishantha.jfr.flamegraph.output;

import com.beust.jcommander.IStringConverter;

/**
 * Different types of events possibly available in a JFR recording.
 */
public enum EventType {

    EVENT_METHOD_PROFILING_SAMPLE("Method Profiling Sample"), 
    EVENT_ALLOCATION_IN_NEW_TLAB("Allocation in new TLAB", true), 
    EVENT_ALLOCATION_OUTSIDE_TLAB("Allocation outside TLAB", true), 
    EVENT_JAVA_EXCEPTION("Java Exception"), 
    EVENT_JAVA_MONITOR_BLOCKED("Java Monitor Blocked");

    /** Name as declared in the JFR recording */
    private final String name;

    /** True if the event is allocation-related */
    private final boolean isAllocation;

    EventType(String name, boolean isAllocation) {
        this.name = name;
        this.isAllocation = isAllocation;
    }

    EventType(String name) {
        this(name, false);
    }

    @Override
    public String toString() {
        return name;
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
        case "locks":
            return EVENT_JAVA_MONITOR_BLOCKED;
        default:
            return EVENT_METHOD_PROFILING_SAMPLE;
        }
    }

    public static final class EventTypeConverter implements IStringConverter<EventType> {
        @Override
        public EventType convert(String value) {
            return EventType.from(value);
        }
    }
}
