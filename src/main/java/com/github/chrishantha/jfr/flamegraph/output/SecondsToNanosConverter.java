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
