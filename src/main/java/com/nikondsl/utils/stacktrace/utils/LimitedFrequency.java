package com.nikondsl.utils.stacktrace.utils;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * This class can be used as simple Timer, for instance to log some info only 1 time per second
 * USAGE:
 * LimitedFrequency lf=new LimitedFrequency(1000L);
 * if (lf.isTimePassed()) logger.info("some limited information to be logged once per second");
 */
public class LimitedFrequency implements Serializable {
    public static LimitedFrequency createOncePerSecond() {
        return new LimitedFrequency();
    }

    public static LimitedFrequency createOncePerTenSeconds() {
        return new LimitedFrequency(10, TimeUnit.SECONDS);
    }

    public static LimitedFrequency createOncePerMinute() {
        return new LimitedFrequency(1, TimeUnit.MINUTES);
    }

    public static LimitedFrequency createOncePerHour() {
        return new LimitedFrequency(1, TimeUnit.HOURS);
    }

    private volatile long lastAccessTime;
    private final long delayTime;

    public LimitedFrequency() {
        this(1, TimeUnit.SECONDS);
    }

    public LimitedFrequency(long value, TimeUnit timeUnit){
        Objects.requireNonNull(timeUnit);
        if (value > 0) {
            this.delayTime = timeUnit.toMillis(value);
        } else {
            throw new IllegalArgumentException("delay should be more than 0, but was " + value);
        }
    }

    public LimitedFrequency(long delayTime){
        this.delayTime = delayTime;
    }

    public boolean isTimePassed(){
        long currentTime = System.nanoTime() / 1_000_000L;
        if (lastAccessTime == 0) {
            lastAccessTime = currentTime;
            return false;
        }
        if (lastAccessTime < currentTime - delayTime){
            lastAccessTime = currentTime;
            return true;
        }
        return false;
    }

    public long get() {
        return System.nanoTime() / 1_000_000L - delayTime;
    }
}