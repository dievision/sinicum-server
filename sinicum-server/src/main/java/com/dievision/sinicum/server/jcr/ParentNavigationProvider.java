package com.dievision.sinicum.server.jcr;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParentNavigationProvider extends NavigationProviderBase {
    private final List<String> properties;
    private static final Logger logger = LoggerFactory.getLogger(ParentNavigationProvider.class);

    public ParentNavigationProvider(String baseNode, List<String> properties) throws
            RepositoryException, ItemNotFoundException, PathNotFoundException {
        this.baseNode = findBaseNode(baseNode);
        this.properties = properties;
    }

    @Override
    protected List<NavigationElement> findNavigationElements(Node baseNode)
        throws RepositoryException {
        List<NavigationElement> navigationElements = new ArrayList<NavigationElement>();
        Node currentNode = baseNode;
        do {
            NavigationElement el = new NavigationElement(currentNode, properties);
            navigationElements.add(0, el);
            currentNode = currentNode.getParent();
        } while (currentNode.getDepth() > 0);
        return  navigationElements;
    }
}
