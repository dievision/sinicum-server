package com.dievision.sinicum.server.magnolia;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dievision.sinicum.server.mgnlAdapters.ServerConfigurationAdapter;
import com.dievision.sinicum.server.mgnlAdapters.TaskAdapter;

/**
 *
 */
public abstract class AbstractInstallTask implements TaskAdapter {
    private static final String MODULE_PATH = "/modules/sinicum-server";
    private ServerConfigurationAdapter serverConfigurationAdapter;

    private static final Logger logger = LoggerFactory.getLogger(AbstractInstallTask.class);

    protected boolean hasChildNode(Node filterNode, String relativePath)
        throws RepositoryException {
        boolean exists = true;
        try {
            filterNode.getNode(relativePath);
        } catch (PathNotFoundException e) {
            exists = false;
        }
        return exists;
    }

    @Override
    public void setServerConfiguration(ServerConfigurationAdapter serverConfigurationAdapter) {
        this.serverConfigurationAdapter = serverConfigurationAdapter;
    }

    protected ServerConfigurationAdapter getServerConfiguration() {
        return this.serverConfigurationAdapter;
    }

    protected String getModuleBasePath() {
        return MODULE_PATH;
    }
}
