package com.dievision.sinicum.server.jcr;


import javax.jcr.AccessDeniedException;
import javax.jcr.Item;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class NodeResolver {
    private Session session;
    private Node node;
    private String path;
    private String uuid;
    private String[] includeChildNodeTypes;
    private static final String[] MGNL5_MIXIN_TYPES = {"mgnl:deleted", "mgnl:lastModified",
        "mgnl:activatable", "mgnl:created", "mgnl:renderable", "mgnl:versionable"};

    private static final Logger logger = LoggerFactory.getLogger(NodeResolver.class);

    public NodeResolver(Node node) {
        this.node = node;
        if (node != null) {
            try {
                this.session = node.getSession();
            } catch (RepositoryException e) {
                // nothing
            }
        }
    }

    public NodeResolver(Session session) {
        this.session = session;
    }

    public NodeApiWrapper getNode() throws RepositoryException, ItemNotFoundException,
            PathNotFoundException, AccessDeniedException {
        if (node == null) {
            node = resolveNode();
        }
        if (isMagnolia5Node(node)) {
            return new NodeApiWrapper5(node, node.getPrimaryNodeType());
        } else {
            return new NodeApiWrapper4(node, node.getPrimaryNodeType());
        }
    }

    private Node resolveNode() throws RepositoryException {
        Node node = null;
        Item item;
        if (uuid == null) {
            item = this.session.getItem(path);
        } else {
            item = this.session.getNodeByIdentifier(uuid);
        }
        if (item.isNode()) {
            node = (Node) item;
        } else {
            throw new ItemNotFoundException("Item '" + path + "' is not a node.");
        }
        return node;
    }

    public void setPath(String path) {
        this.uuid = null;
        this.path = path;
    }

    public void setUuid(String uuid) {
        this.path = null;
        this.uuid = uuid;
    }

    public void setIncludeChildNodeTypes(String... includeChildNodeTypes) {
        this.includeChildNodeTypes = includeChildNodeTypes;
    }

    private boolean isMagnolia5Node(Node node) throws RepositoryException {
        boolean magnolia5Node = false;
        for (String mixinType : MGNL5_MIXIN_TYPES) {
            if (node.isNodeType(mixinType)) {
                magnolia5Node = true;
                break;
            }
        }
        return magnolia5Node;
    }
}
