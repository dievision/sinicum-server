package com.dievision.sinicum.server.jcr.caching;

import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CachingEventListener implements EventListener {
    private static final Logger logger = LoggerFactory.getLogger(CachingEventListener.class);

    public void onEvent(EventIterator events) {
        if (events.hasNext()) {
            Event event = events.nextEvent();
            try {
                if (!event.getPath().startsWith("/jcr:system")) {
                    String sitePrefix = returnSitePrefix(event.getPath());
                    GlobalRepositoryState.updateCacheKey(sitePrefix);
                }
            } catch (RepositoryException e) {
                e.printStackTrace();
            }
            GlobalRepositoryState.updateCacheKey();
        }
    }

    private String returnSitePrefix(String path) {
        String[] parts = path.split("/");
        return parts[1];
    }
}
