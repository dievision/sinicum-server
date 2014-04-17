package com.dievision.sinicum.server.magnolia;

import java.net.URI;
import java.net.URISyntaxException;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dievision.sinicum.server.filters.ProxyFilterConfig;
import com.dievision.sinicum.server.jcr.caching.CacheObserverManager;

/**
 *
 */
public class SinicumServerModuleBase {
    private static final Logger logger = LoggerFactory.getLogger(SinicumServerModuleBase.class);

    protected void start(String moduleName, Session session) {
        readModuleConfiguration(moduleName, session);
        registerCacheObservers();
    }

    private void readModuleConfiguration(String moduleName, Session session) {
        logger.info("Configuring sinicum-server module...");
        try {
            Node node = (Node) session.getItem("/modules/" + moduleName
                    + "/config/proxy-servlet/default");
            if (node.hasProperty("proxyPathPattern")) {
                ProxyFilterConfig.getInstance().setProxyPattern(
                        node.getProperty("proxyPathPattern").getString());
            }
            if (node.hasProperty("proxyTargetUri")) {
                try {
                    ProxyFilterConfig.getInstance().setProxyTargetUri(
                            new URI(node.getProperty("proxyTargetUri").getString()));
                } catch (URISyntaxException e) {
                    logger.error("Error setting new sinicum-server proxy target: " + e.toString());
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
