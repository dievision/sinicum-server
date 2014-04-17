package com.dievision.sinicum.server.jcr;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NavigationProvider extends NavigationProviderBase {
    private final int depth;
    private final List<String> properties;

    private static final String PAGE_TYPE = "mgnl:page";
    private static final Logger logger = LoggerFactory.getLogger(NavigationProvider.class);

    public NavigationProvider(String baseNodeUuidOrPath, List<String> properties, int depth)
        throws RepositoryException, ItemNotFoundException, PathNotFoundException {
        baseNode = findBaseNode(baseNodeUuidOrPath);
        this.depth = depth;
        this.properties = properties;
    }

    protected List<NavigationElement> findNavigationElements(Node node) throws RepositoryException {
        List<NavigationElement> elements = new ArrayList<NavigationElement>();
        NodeIterator iterator = node.getNodes();
        while (iterator.hasNext()) {
            Node child = iterator.nextNode();
            if (PAGE_TYPE.equals(child.getPrimaryNodeType().getName())) {
                NavigationElement element = new NavigationElement(child, properties);
                if (child.getDepth() <= maximumAllowedDepth()) {
                    element.setChildren(findNavigationElements(child));
                }
                elements.add(element);
            }
        }
        return elements;
    }

    private int maximumAllowedDepth() throws RepositoryException {
        return baseNode.getDepth() + depth - 1;
    }
}
