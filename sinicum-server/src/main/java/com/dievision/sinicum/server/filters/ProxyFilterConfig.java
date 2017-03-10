package com.dievision.sinicum.server.filters;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

public final class ProxyFilterConfig {
    private static ProxyFilterConfig instance = null;
    private ArrayList<ProxyEntry> entries = new ArrayList<ProxyEntry>();
    private static final Logger logger = LoggerFactory.getLogger(ProxyFilterConfig.class);

    private ProxyFilterConfig() {
        // nothing
    }

    public boolean matchesPath(String proxyPath) {
        boolean performProxying = false;
        for (ProxyEntry entry : getProxyEntries()) {
            if (entry.getProxyPattern().matcher(proxyPath).matches()) {
                performProxying = true;
            }
        }
        return performProxying;
    }

    public void addProxyEntry(ProxyEntry entry) {
        entries.add(entry);
    }

    public ArrayList<ProxyEntry> getProxyEntries() {
        return entries;
    }

    public URI getProxyTargetUri(String proxyPath) {
        for (ProxyEntry entry : getProxyEntries()) {
            if (entry.getProxyPattern().matcher(proxyPath).matches()) {
                return entry.getProxyTargetUri();
            }
        }
        return null;
    }

    public static ProxyFilterConfig getInstance() {
        if (instance == null) {
            synchronized (ProxyFilterConfig.class) {
                if (instance == null) {
                    ProxyFilterConfig proxyFilterConfig = new ProxyFilterConfig();

                    instance = proxyFilterConfig;
                }
            }
        }
        return instance;
    }

    public static ProxyEntry buildProxyEntry(Node node) throws RepositoryException {
        ProxyEntry entry = new ProxyEntry();
        if (node.hasProperty("proxyPathPattern")) {
            entry.setProxyPattern(node.getProperty("proxyPathPattern").getString());
        }
        if (node.hasProperty("proxyTargetUri")) {
            try {
                entry.setProxyTargetUri(new URI(node.getProperty("proxyTargetUri").getString()));
            } catch (URISyntaxException e) {
                logger.error("Error setting new sinicum-server proxy target: " + e.toString());
            }
        }
        return entry;
    }

}
