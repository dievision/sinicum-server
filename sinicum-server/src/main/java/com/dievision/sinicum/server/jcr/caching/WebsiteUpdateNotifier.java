package com.dievision.sinicum.server.jcr.caching;


import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.jcr.query.Query;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WebsiteUpdateNotifier implements EventListener {
    private static final Logger logger = LoggerFactory.getLogger(WebsiteUpdateNotifier.class);
    private static final String DEFAULT_DOMAIN_KEY = "__DEFAULT__";
    private Map<String, Long> lastUpdated = new HashMap<>();
    private Set<MultisiteDefinition> multisiteDefinitions = null;
    private String targetServerUrl;
    private String targetServerAuthToken;


    public WebsiteUpdateNotifier(String targetServerUrl, String targetServerAuthToken) {
        this.targetServerUrl = targetServerUrl;
        this.targetServerAuthToken = targetServerAuthToken;
        startUpdaterService();
    }

    public void onEvent(EventIterator events) {
        if (events.hasNext()) {
            Event event = events.nextEvent();
            try {
                String domainKey = findMultisiteDomainKey(event.getPath());
                lastUpdated.put(domainKey, System.currentTimeMillis());
            } catch (RepositoryException e) {
                e.printStackTrace();
            }
        }
    }

    private void startUpdaterService() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleWithFixedDelay(new Updater(), 10, 3, TimeUnit.SECONDS);
    }

    private String findMultisiteDomainKey(String path) {
        if (path != null && !"".equals(path)) {
            for (MultisiteDefinition def : multisiteDefinitions) {
                if (path.startsWith(def.getRootPath())) {
                    return def.getPrimaryDomain();
                }
            }
        }
        return DEFAULT_DOMAIN_KEY;
    }

    public void findMultisiteDefinitions(Session multisiteSession) {
        String stmt = "select * from [mgnl:multisite]";
        Set<MultisiteDefinition> domainNames = new HashSet<>();
        try {
            Query query = multisiteSession.getWorkspace().getQueryManager()
                    .createQuery(stmt, Query.JCR_SQL2);
            NodeIterator iter = query.execute().getNodes();
            while (iter.hasNext()) {
                Node node = iter.nextNode();
                String domain = node.getProperty("primary_domain").getString();
                String rootPath = node.getProperty("root_node").getString();
                if (domain != null && !"".equals(domain)
                        && rootPath != null && !"".equals(rootPath)) {
                    domainNames.add(new MultisiteDefinition(domain, rootPath));
                }
            }
        } catch (RepositoryException e) {
            logger.error("Error getting multisite domains: " + e.toString());
        }
        multisiteDefinitions = domainNames;
    }

    private class Updater implements Runnable {
        private Map<String, Long> lastNotified = new HashMap<>();
        private HttpClient httpClient = HttpClients.createDefault();

        public void run() {
            try {
                sendUpdateRequestIfNecessary(System.currentTimeMillis());
            } catch (Exception e) {
                logger.error("Error executing website update notifier: " + e.getMessage());
            }
        }

        private void sendUpdateRequestIfNecessary(Long time) {
            for (String namespace : lastUpdated.keySet()) {
                Long updated = lastUpdated.get(namespace);
                Long notified = lastNotified.get(namespace);
                if (notified == null || notified < updated) {
                    boolean success = sendUpdateForNamespace(namespace);
                    if (success) {
                        lastNotified.put(namespace, time);
                    }
                }
            }
        }

        private boolean sendUpdateForNamespace(String namespace) {
            try {
                logger.info("Starting update");
                String effectiveNamespace = null;
                if (!DEFAULT_DOMAIN_KEY.equals(namespace)) {
                    effectiveNamespace = namespace;
                }
                URI uri = createUri(effectiveNamespace);
                HttpDelete delete = new HttpDelete(uri);
                delete.setHeader("Content-Type", "application/json");
                delete.setHeader("Accept", "application/json");
                delete.setHeader("Auth", targetServerAuthToken);
                logger.info("Sending update to " + uri.toString());
                HttpResponse response = httpClient.execute(delete);
                if (response.getStatusLine().getStatusCode() == 200) {
                    logger.info("Update successful for namespace " + namespace);
                    return true;
                } else {
                    logger.error("Error sending update notification: "
                            + response.getStatusLine().getReasonPhrase());
                    return false;
                }
            } catch (Exception e) {
                logger.error("Error sending update notification: " + e.getMessage());
                return false;
            }
        }

        private URI createUri(String namespace) throws URISyntaxException {
            URIBuilder builder = new URIBuilder(targetServerUrl)
                    .setPath("/_sinicum/cache");
            if (namespace != null) {
                builder = builder.setParameter("namespace", namespace);
            }
            return builder.build();
        }
    }

    private static class MultisiteDefinition {
        private String primaryDomain;
        private String rootPath;

        MultisiteDefinition(String primaryDomain, String rootPath) {
            this.primaryDomain = extractHostName(primaryDomain);
            this.rootPath = rootPath;
        }

        String getPrimaryDomain() {
            return primaryDomain;
        }

        String getRootPath() {
            return rootPath;
        }

        private String extractHostName(String primaryDomain) {
            try {
                URI uri = new URI(primaryDomain);
                return uri.getHost();
            } catch (URISyntaxException e) {
                return primaryDomain;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            MultisiteDefinition that = (MultisiteDefinition) o;
            return Objects.equals(primaryDomain, that.primaryDomain)
                    && Objects.equals(rootPath, that.rootPath);
        }

        @Override
        public int hashCode() {
            return Objects.hash(primaryDomain, rootPath);
        }
    }

}
