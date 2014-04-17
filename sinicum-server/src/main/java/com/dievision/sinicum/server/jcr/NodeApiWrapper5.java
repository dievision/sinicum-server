package com.dievision.sinicum.server.jcr;

import javax.jcr.Node;
import javax.jcr.nodetype.NodeType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeApiWrapper5 extends NodeApiWrapper4 {
    private static final String[] MGNL5_META_JCR_PROPERTIES = {"mgnl:deleted", "mgnl:deletedBy",
        "mgnl:lastModified", "mgnl:lastModifiedBy",
        "mgnl:lastActivated", "mgnl:lastActivatedBy", "mgnl:activationStatus",
        "mgnl:created", "mgnl:createdBy",
        "mgnl:template",
        "mgnl:comment"};
    private static final Logger logger = LoggerFactory.getLogger(NodeApiWrapper5.class);

    public NodeApiWrapper5(Node node, NodeType primaryNodeType,
            String... includeChildNodeTypes) {
        super(node, primaryNodeType, includeChildNodeTypes);
    }

    @Override
    public NodeApiWrapperMeta getMeta() {
        return new NodeApiWrapper5Meta(getNode());
    }

    @Override
    protected NodeApiWrapper createNodeApiWrapper(Node node, NodeType primaryNodeType) {
        return new NodeApiWrapper5(node, primaryNodeType);
    }

    @Override
    protected boolean includePropertyInProperties(String propertyName) {
        boolean includeProperty = super.includePropertyInProperties(propertyName);
        if (includeProperty) {
            for (String property : MGNL5_META_JCR_PROPERTIES) {
                if (property.equals(propertyName)) {
                    includeProperty = false;
                    break;
                }
            }
        }
        return includeProperty;
    }
}
