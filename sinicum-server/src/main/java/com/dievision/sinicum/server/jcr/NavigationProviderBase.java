package com.dievision.sinicum.server.jcr;

import java.util.List;
import java.util.UUID;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dievision.sinicum.server.mgnlAdapters.MgnlContextAdapter;

public abstract class NavigationProviderBase {
    protected Node baseNode;
    private Session session;
    private List<NavigationElement> navigationElements;
    private static final String WEBSITE = "website";
    private static final Logger logger = LoggerFactory.getLogger(NavigationProviderBase.class);

    protected abstract List<NavigationElement> findNavigationElements(Node baseNode)
        throws RepositoryException;

    public List<NavigationElement> getNavigationElements() throws RepositoryException {
        if (navigationElements == null) {
            navigationElements = findNavigationElements(baseNode);
        }
        return navigationElements;
    }

    protected Node findBaseNode(String baseNodeUuidOrPath) throws RepositoryException,
                PathNotFoundException {
        Node result;
        try {
            // test for UUID format
            UUID.fromString(baseNodeUuidOrPath);
            result = getSession().getNodeByIdentifier(baseNodeUuidOrPath);
        } catch (Exception e) {
            // else try by path
            result = getSession().getRootNode().getNode(baseNodeUuidOrPath);
        }
        return result;
    }

    protected Session getSession() throws RepositoryException {
        if (session == null) {
            session = MgnlContextAdapter.getJcrSession(WEBSITE);
        }
        return session;
    }


}
