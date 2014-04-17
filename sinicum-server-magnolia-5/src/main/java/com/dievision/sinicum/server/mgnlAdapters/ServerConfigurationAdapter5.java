package com.dievision.sinicum.server.mgnlAdapters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.objectfactory.Components;

public class ServerConfigurationAdapter5 implements ServerConfigurationAdapter {
    ServerConfiguration serverConfiguration;
    private static final Logger logger =
            LoggerFactory.getLogger(ServerConfigurationAdapter5.class);

    public ServerConfigurationAdapter5() {
        this.serverConfiguration = Components.getComponent(ServerConfiguration.class);
    }

    @Override
    public boolean isAdmin() {
        return serverConfiguration.isAdmin();
    }
}
