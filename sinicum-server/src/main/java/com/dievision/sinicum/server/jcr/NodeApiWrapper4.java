package com.dievision.sinicum.server.jcr;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class NodeApiWrapper4 implements NodeApiWrapper {
    private final Node node;
    private final NodeType primaryNodeType;
    private final String[] includeChildNodeTypes;
    private final PropertyToJsonTypeTranslator translator = new PropertyToJsonTypeTranslator();

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

    @JsonIgnore
    public Node getNode() {
        return this.node;
    }

    public NodeApiWrapperMeta getMeta() {
        return new NodeApiWrapper4Meta(node);
    }

    public Map<String, Object> getProperties() throws RepositoryException {
        Map<String, Object> propertyMap = new LinkedHashMap<String, Object>();
        PropertyIterator properties = node.getProperties();
        while (properties.hasNext()) {
            Property prop = properties.nextProperty();
            String propertyName = prop.getName();
            if (includePropertyInProperties(propertyName)) {
                propertyMap.put(propertyName, translator.resolvePropertyToJsonType(prop));
            }
        }
        return propertyMap;
    }

    public Map<String, Object> getNodes() throws RepositoryException {
        Map<String, Object> nodeMap = new LinkedHashMap<String, Object>();
        NodeIterator nodes = node.getNodes();
        while (nodes.hasNext()) {
            Node child = nodes.nextNode();
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
