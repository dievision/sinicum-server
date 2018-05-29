package com.dievision.sinicum.server.jcr.caching;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.EventListenerIterator;
import javax.jcr.observation.ObservationManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dievision.sinicum.server.mgnlAdapters.MgnlContextAdapter;

public class CacheObserverManager {
    private static final String[] OBSERVED_WORKSPACES = {"data", "dms", "website"};
    private static final Logger logger = LoggerFactory.getLogger(CacheObserverManager.class);

    public void registerObservers() {
        for (String workspace : OBSERVED_WORKSPACES) {
            Session session = null;
            try {
                session = MgnlContextAdapter.getJcrSession(workspace);
            } catch (RepositoryException e) {
                logger.warn("Could not get session for workspace " + workspace + ": "
                        + e.toString());
            }
            if (session != null) {
                try {
                    registerObserver(session);
                } catch (RepositoryException e) {
                    logger.error("Could not register caching observer: " + e.toString());
                }
            }
        }
    }

    private void registerObserver(Session session) throws RepositoryException {
        ObservationManager observationManager = session.getWorkspace().getObservationManager();
        if (hasNoCachingEventListener(observationManager)) {
            EventListener listener = new CachingEventListener();
            observationManager.addEventListener(listener, org.apache.jackrabbit.spi.Event.ALL_TYPES,
                    "/", true, null, null, true);
        }
    }

    private boolean hasNoCachingEventListener(ObservationManager observationManager)
        throws RepositoryException {
        EventListenerIterator iterator = observationManager.getRegisteredEventListeners();
        while (iterator.hasNext()) {
            EventListener listener = iterator.nextEventListener();
            if (listener instanceof CachingEventListener) {
                return false;
            }
        }
        return true;
    }
}
