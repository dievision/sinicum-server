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
    private static final String[] OBSERVED_WORKSPACES = {"config", "data", "dms", "website"};
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
                    registerCachingEventListener(session);
                    if ("website".equals(workspace)) {
                        registerUpdateNotifier(session);
                    }
                } catch (RepositoryException e) {
                    logger.error("Could not register caching observer: " + e.toString());
                }
            }
        }
    }

    private void registerCachingEventListener(Session session) throws RepositoryException {
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

    private void registerUpdateNotifier(Session session) throws RepositoryException {
        String targetServerUrl = System.getenv("SINICUM_TARGET_SERVER_URL");
        String targetServerAuthToken = System.getenv("SINICUM_TARGET_SERVER_AUTH_TOKEN");
        if (targetServerUrl == null || targetServerAuthToken == null) {
            logger.warn("Update notifier not configured, not performing update notifications");
            return;
        }
        ObservationManager observationManager = session.getWorkspace().getObservationManager();
        if (hasNoUpdateNotifier(observationManager)) {
            WebsiteUpdateNotifier listener = new WebsiteUpdateNotifier(
                    targetServerUrl, targetServerAuthToken);
            Session multisiteSession;
            try {
                multisiteSession = MgnlContextAdapter.getJcrSession("multisite");
                listener.findMultisiteDefinitions(multisiteSession);
            } catch (RepositoryException e) {
                logger.warn("Could not find multisite definition, "
                        + "not initializing update notifier namespaces: " + e.getMessage());
            }
            observationManager.addEventListener(listener, org.apache.jackrabbit.spi.Event.ALL_TYPES,
                "/", true, null, null, true);
        }
    }

    private boolean hasNoUpdateNotifier(ObservationManager observationManager)
            throws RepositoryException {
        EventListenerIterator iterator = observationManager.getRegisteredEventListeners();
        while (iterator.hasNext()) {
            EventListener listener = iterator.nextEventListener();
            if (listener instanceof WebsiteUpdateNotifier) {
                return false;
            }
        }
        return true;
    }
}
