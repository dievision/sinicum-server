package com.dievision.sinicum.server.jcr.caching;

import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CachingEventListener implements EventListener {
    private static final Logger logger = LoggerFactory.getLogger(CachingEventListener.class);

    public void onEvent(EventIterator events) {
        if (events.hasNext()) {
            GlobalRepositoryState.updateCacheKey();
        }
    }
}
