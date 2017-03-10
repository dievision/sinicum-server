package com.dievision.sinicum.server.magnolia;

import com.dievision.sinicum.server.filters.ProxyEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dievision.sinicum.server.filters.ProxyFilterConfig;
import com.dievision.sinicum.server.jcr.caching.CacheObserverManager;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

public class SinicumServerModuleBase {
    private static final Logger logger = LoggerFactory.getLogger(SinicumServerModuleBase.class);

    protected void start(String moduleName, Session session) {
        readModuleConfiguration(moduleName, session);
        registerCacheObservers();
    }

    private void readModuleConfiguration(String moduleName, Session session) {
        logger.info("Configuring sinicum-server module...");
        try {
            if (session.nodeExists("/modules/" + moduleName
                    + "/config/proxy-servlet/default")) {
                Node node = (Node) session.getItem("/modules/" + moduleName
                        + "/config/proxy-servlet/default");
                ProxyEntry entry = ProxyFilterConfig.buildProxyEntry(node);
                ProxyFilterConfig.getInstance().addProxyEntry(entry);
            } else {
                Node parent = session.getNode("/modules/" + moduleName + "/config/proxy-servlet");
                if (parent.hasNodes()) {
                    NodeIterator it = parent.getNodes();
                    while (it.hasNext()) {
                        Node node = it.nextNode();
                        ProxyEntry entry = ProxyFilterConfig.buildProxyEntry(node);
                        ProxyFilterConfig.getInstance().addProxyEntry(entry);
                    }
                }
            }
            logger.info("Success");
        } catch (PathNotFoundException e) {
            logger.info("Could not find sinicum-server config node: " + e.toString());
        } catch (RepositoryException e) {
            logger.error("Error configuring sincium-server module: " + e.toString());
        }
    }

    private void registerCacheObservers() {
        CacheObserverManager observerManager = new CacheObserverManager();
        observerManager.registerObservers();
    }
}
