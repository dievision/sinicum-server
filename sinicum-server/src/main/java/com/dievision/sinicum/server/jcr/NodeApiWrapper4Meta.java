package com.dievision.sinicum.server.jcr;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeApiWrapper4Meta implements NodeApiWrapperMeta {
    private final Node node;

    private static final String MIX_REFERENCABLE = "mix:referenceable";
    private static final Logger logger = LoggerFactory.getLogger(NodeApiWrapper4Meta.class);

    public NodeApiWrapper4Meta(Node node) {
        this.node = node;
    }

    @Override
    public String getWorkspace() throws RepositoryException {
        return node.getSession().getWorkspace().getName();
    }

    @Override
    public String getJcrUuid() throws RepositoryException {
        String uuid = null;
        if (node.isNodeType(MIX_REFERENCABLE)) {
            uuid = node.getUUID();
        }
        return uuid;
    }

    @Override
    public String getPath() throws RepositoryException {
        return node.getPath();
    }

    @Override
    public String getName() throws RepositoryException {
        return node.getName();
    }

    @Override
    public String getJcrPrimaryType() throws RepositoryException {
        return node.getPrimaryNodeType().getName();
    }

    @Override
    public List<String> getSuperTypes() throws RepositoryException {
        List<String> types = new ArrayList<String>();
        for (NodeType superType : node.getPrimaryNodeType().getSupertypes()) {
            types.add(superType.getName());
        }
        return types;
    }

    @Override
    public int getDepth() throws RepositoryException {
        return node.getDepth();
    }

    @Override
    public String getJcrCreated() throws RepositoryException {
        String created = null;
        if (node.hasProperty("jcr:created")) {
            created = node.getProperty("jcr:created").getString();
        }
        return created;
    }

    @Override
    public List<String> getMixinNodeTypes() throws RepositoryException {
        List<String> mixinTypes = new ArrayList<String>();
        for (NodeType nodeType : node.getMixinNodeTypes()) {
            mixinTypes.add(nodeType.getName());
        }
        return mixinTypes;
    }

    protected Node getNode() {
        return node;
    }
}
