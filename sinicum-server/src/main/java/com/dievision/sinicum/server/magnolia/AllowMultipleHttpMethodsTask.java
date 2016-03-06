package com.dievision.sinicum.server.magnolia;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dievision.sinicum.server.mgnlAdapters.TaskAdapterExecutionException;

/**
 * Sets up Magnolia to allow all the usual HTTP methods.
 */
public class AllowMultipleHttpMethodsTask extends AbstractInstallTask {
    private static final String NODE_PATH = "/server/IPConfig/allow-all";
    private static final String ALLOW_VALUES = "GET,POST,PUT,PATCH,DELETE";
    private static final Logger logger = LoggerFactory.getLogger(
            AllowMultipleHttpMethodsTask.class);

    public String getName() {
        return "Allow multiple HTTP Methods";
    }

    public String getDescription() {
        return "Configures Magnolia's IP configuration to allow the GET, POST, PUT, PATCH and "
                + "DELETE HTTP methods";
    }

    public void execute(Session configSession) throws TaskAdapterExecutionException {
        try {
            allowAllHttpMethods(configSession);
        } catch (RepositoryException e) {
            throw new TaskAdapterExecutionException(e.getMessage(), e);
        }
    }

    private void allowAllHttpMethods(Session session) throws RepositoryException {
        Item ipConfig = session.getItem(NODE_PATH);
        if (ipConfig != null && ipConfig.isNode()) {
            Node ipConfigNode = (Node) ipConfig;
            if (ipConfigNode.hasProperty("methods")
                    && !ALLOW_VALUES.equals(ipConfigNode.getProperty("methods").getString())) {
                ipConfigNode.setProperty("methods", ALLOW_VALUES);
                ipConfigNode.getSession().save();
            }
        }
    }
}
