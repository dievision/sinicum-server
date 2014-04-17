package com.dievision.sinicum.server.mgnlAdapters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TaskAdapterExecutionException extends Exception {
    private static final Logger logger =
            LoggerFactory.getLogger(TaskAdapterExecutionException.class);

    public TaskAdapterExecutionException() {
        super();
    }

    public TaskAdapterExecutionException(String s) {
        super(s);
    }

    public TaskAdapterExecutionException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public TaskAdapterExecutionException(Throwable throwable) {
        super(throwable);
    }
}
