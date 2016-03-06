package com.dievision.sinicum.server.magnolia;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dievision.sinicum.server.mgnlAdapters.NodeTypeAdapter;
import com.dievision.sinicum.server.mgnlAdapters.TaskAdapterExecutionException;

/**
 *
 */
public class SinicumProxyFilterInstallTask extends AbstractInstallTask {
    private static final String CMS_FILTER_PATH = "/server/filters/cms";
    private static final String FILTER_NODE_NAME = "sinicumProxyFilter";
    private static final String PROXY_FILTER_CLASS_NAME =
            "com.dievision.sinicum.server.filters.SinicumProxyFilter";
    private static final Logger logger =
            LoggerFactory.getLogger(SinicumProxyFilterInstallTask.class);

    public String getName() {
        return "SinicumProxyFilter Install Task";
    }

    public String getDescription() {
        return "Installs the Sinicum Proxy Filter";
    }

    public void execute(Session configSession) throws TaskAdapterExecutionException {
        try {
            installSinicumProxyFilter(configSession);
        } catch (RepositoryException e) {
            throw new TaskAdapterExecutionException(e.toString(), e);
        }
    }

    private void installSinicumProxyFilter(Session session) throws RepositoryException {
        Item filterItem = session.getItem(CMS_FILTER_PATH);
        if (filterItem != null && filterItem.isNode()) {
            Node filterNode = (Node) filterItem;
            if (!hasChildNode(filterNode, FILTER_NODE_NAME)) {

                // create Node
                Node proxyFilter = filterNode.addNode(FILTER_NODE_NAME,
                        NodeTypeAdapter.getContentType());
                proxyFilter.setProperty("class", PROXY_FILTER_CLASS_NAME);
                proxyFilter.setProperty("enabled", getServerAdmin(session));
                Node bypasses = proxyFilter.addNode("bypasses",
                        NodeTypeAdapter.getContentNodeType());
                installBypassesNode(bypasses, "resources", "/.resources");
                installBypassesNode(bypasses, "magnolia", "/.magnolia");
                installBypassesNode(bypasses, "dataModule", "/dataModule");

                // set node after the interceptor or as first filter node
                Node beforeNode = null;
                try {
                    beforeNode = filterNode.getNode("modelExecution");
                } catch (RepositoryException e) {
                    // nothing
                }
                if (beforeNode == null) {
                    NodeIterator childNodes = filterNode.getNodes();
                    if (childNodes.hasNext()) {
                        beforeNode = childNodes.nextNode();
                    }
                }
                filterNode.orderBefore(proxyFilter.getName(), beforeNode.getName());
                session.save();
            }
        }
    }

    private void installBypassesNode(Node bypasses, String name, String pattern)
        throws RepositoryException {
        Node bypassNode = bypasses.addNode(name, NodeTypeAdapter.getContentNodeType());
        bypassNode.setProperty("class", "info.magnolia.voting.voters.URIStartsWithVoter");
        bypassNode.setProperty("pattern", pattern);
    }

    /**
     * Find out if the server instance is an Admin instance. ServerConfiguration#isAdmin() does
     * not seem to work at this stage.
     */
    private boolean getServerAdmin(Session session) throws RepositoryException {
        boolean isAdmin = false;
        try {
            Node server = session.getRootNode().getNode("server");
            if (server.hasProperty("admin")) {
                isAdmin = server.getProperty("admin").getBoolean();
            }
        } catch (PathNotFoundException e) {
            // nothing
        }
        return isAdmin;
    }
}
