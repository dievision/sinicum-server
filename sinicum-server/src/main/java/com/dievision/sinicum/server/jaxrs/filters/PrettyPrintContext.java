package com.dievision.sinicum.server.jaxrs.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PrettyPrintContext {
    private static ThreadLocal<Boolean> usePrettyPrint = new ThreadLocal<Boolean>();
    private static PrettyPrintContext instance;
    private static final Logger logger = LoggerFactory.getLogger(PrettyPrintContext.class);

    private PrettyPrintContext() {
        // nothing
    }

    public void setPrettyPrint(boolean prettyPrint) {
        usePrettyPrint.set(prettyPrint);
    }

    public boolean getPrettyPrint() {
        Boolean result = usePrettyPrint.get();
        return result == null ? false : result;
    }

    public static PrettyPrintContext getInstance() {
        if (instance == null) {
            synchronized (PrettyPrintContext.class) {
                if (instance == null) {
                    instance = new PrettyPrintContext();
                }
            }
        }
        return instance;
    }
}
