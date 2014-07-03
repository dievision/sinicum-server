package com.dievision.sinicum.server.jcr;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class NodeApiWrapper4 implements NodeApiWrapper {
    private final Node node;
    private final NodeType primaryNodeType;
    private final String[] includeChildNodeTypes;
    private final WysiwygTemplateTranslator translator = new WysiwygTemplateTranslator();

    protected static final String[] MGNL4_META_JCR_PROPERTIES = {"jcr:uuid", "jcr:primaryType",
        "mixinTypes", "jcr:mixinTypes", "jcr:created", "jcr:createdBy"};
    private static final String MGNL_WORKSPACE_WORKAROUND = "website";
    private static final String MGNL_PAGE_WORKAROUND = "mgnl:page";
    private static final Logger logger = LoggerFactory.getLogger(NodeApiWrapper4.class);

    public NodeApiWrapper4(Node node, NodeType primaryNodeType, String... includeChildNodeTypes) {
        this.node = node;
        this.primaryNodeType = primaryNodeType;
        this.includeChildNodeTypes = includeChildNodeTypes;
    }

    @Override
    @JsonIgnore
    public Node getNode() {
        return this.node;
    }

    @Override
    public NodeApiWrapperMeta getMeta() {
        return new NodeApiWrapper4Meta(node);
    }

    @Override
    public Map<String, Object> getProperties() throws RepositoryException {
        Map<String, Object> propertyMap = new LinkedHashMap<String, Object>();
        PropertyIterator properties = node.getProperties();
        while (properties.hasNext()) {
            Property prop = properties.nextProperty();
            String propertyName = prop.getName();
            if (includePropertyInProperties(propertyName)) {
                int type = prop.getType();
                if (type == PropertyType.STRING) {
                    propertyMap.put(prop.getName(), translator.translate(prop.getString()));
                } else if (type == PropertyType.DOUBLE) {
                    propertyMap.put(propertyName, prop.getDouble());
                } else if (type == PropertyType.LONG) {
                    propertyMap.put(propertyName, prop.getLong());
                } else if (type == PropertyType.BOOLEAN) {
                    propertyMap.put(propertyName, prop.getBoolean());
                } else if (type == PropertyType.DATE) {
                    propertyMap.put(propertyName, prop.getString());
                } else if (type == PropertyType.BINARY) {
                    propertyMap.put(propertyName, "Binary Data Type not supported");
                } else if (!prop.getDefinition().isMultiple()) {
                    propertyMap.put(propertyName, prop.getString());
                }
            }
        }
        return propertyMap;
    }

    @Override
    public Map<String, Object> getNodes() throws RepositoryException {
        Map<String, Object> nodeMap = new LinkedHashMap<String, Object>();
        NodeIterator nodes = node.getNodes();
        while (nodes.hasNext()) {
            Node child = nodes.nextNode();
            logger.debug("child " + child.getPrimaryNodeType().getName());
            logger.debug("self  " + recursivePrimaryNodeType(node));
            if (!child.getPrimaryNodeType().getName().equals(recursivePrimaryNodeType(node))) {
                nodeMap.put(child.getName(), createNodeApiWrapper(child, primaryNodeType));
            }
        }
        return nodeMap;
    }

    protected NodeApiWrapper createNodeApiWrapper(Node node, NodeType primaryNodeType) {
        return new NodeApiWrapper4(node, primaryNodeType);
    }

    protected boolean includePropertyInProperties(String propertyName) {
        boolean includeProperty = true;
        for (String property : MGNL4_META_JCR_PROPERTIES) {
            if (property.equals(propertyName)) {
                includeProperty = false;
                break;
            }
        }
        return includeProperty;
    }

    private String recursivePrimaryNodeType(Node node) throws RepositoryException {
        if (MGNL_WORKSPACE_WORKAROUND.equals(node.getSession().getWorkspace().getName())) {
            return MGNL_PAGE_WORKAROUND;
        } else {
            NodeType nodeTypeToUse = node.getPrimaryNodeType();
            if (this.primaryNodeType != null) {
                nodeTypeToUse = this.primaryNodeType;
            }
            return nodeTypeToUse.getName();
        }
    }
}
