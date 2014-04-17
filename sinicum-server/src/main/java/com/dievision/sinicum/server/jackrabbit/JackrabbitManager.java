package com.dievision.sinicum.server.jackrabbit;

import org.apache.jackrabbit.api.JackrabbitRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public final class JackrabbitManager {
    private static final Logger logger = LoggerFactory.getLogger(JackrabbitManager.class);
    private static JackrabbitRepository jackrabbitRepository;

    private JackrabbitManager() {
        // nothing
    }

    public static void registerRepository(JackrabbitRepository repository) {
        jackrabbitRepository = repository;
    }

    public static JackrabbitRepository getRepository() {
        return jackrabbitRepository;
    }

}
