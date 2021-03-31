package com.nikondsl.utils.stacketrace.factory;

import com.nikondsl.utils.stacketrace.StackTraceCompacter;

public class CompacterFactory {
    private StackTraceCompacter instance = create();

    /**
     * Use this method in order to create a new instance of compacter.
     * That will not support for recalling the previous exceptions, but
     * will compact it anyway.
     * @return new instance.
     */
    public static StackTraceCompacter create() {
        return new StackTraceCompacter();
    }

    /**
     * Use this method in order to return created instance in advance.
     * That will support for recalling the previous exceptions, so it
     * will not only compact the current exception, it also will make
     * the same exceptions disappear with only the line to be left:
     *
     * 'Exception (<id>) is thrown #121 times'
     *
     * @return singleton instance.
     */
    public StackTraceCompacter getInstance() {
        return instance;
    }
}
