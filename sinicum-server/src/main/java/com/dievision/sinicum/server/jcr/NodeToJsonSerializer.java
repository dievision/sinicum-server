package com.dievision.sinicum.server.jcr;

import java.io.IOException;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerator;

/**
 *
 */
public class NodeToJsonSerializer {
    private static final String MIX_REFERENCABLE = "mix:referenceable";
    private static final String[] META_JCR_PROPERTIES = {"jcr:uuid", "jcr:primaryType",
        "mixinTypes", "jcr:mixinTypes", "jcr:created"};
    private static final Logger logger = LoggerFactory.getLogger(NodeToJsonSerializer.class);
    JsonGenerator gen;
    Node node;
    NodeType primaryNodeType;

    public NodeToJsonSerializer(JsonGenerator gen, Node node) {
        this.gen = gen;
        this.node = node;
    }

    public void setPrimaryNodeType(NodeType primaryNodeType) {
        this.primaryNodeType = primaryNodeType;
    }

    public void buildJson() throws RepositoryException, IOException {
        serializeMetaData();
        serializeProperties();
        serializeChildNodes();
    }

    private void serializeMetaData() throws IOException, RepositoryException {
        gen.writeObjectFieldStart("meta");
        gen.writeStringField("workspace", node.getSession().getWorkspace().getName());
        String uuid = null;
        if (node.isNodeType(MIX_REFERENCABLE)) {
            uuid = node.getUUID();
        }
        gen.writeStringField("jcr:uuid", uuid);
        gen.writeStringField("path", node.getPath());
        gen.writeStringField("name", node.getName());
        gen.writeStringField("jcr:primaryType", node.getPrimaryNodeType().getName());
        gen.writeFieldName("superTypes");
        gen.writeStartArray();
        for (NodeType superType : node.getPrimaryNodeType().getSupertypes()) {
            gen.writeString(superType.getName());
        }
        gen.writeEndArray();
        gen.writeNumberField("depth", node.getDepth());
        if (node.hasProperty("jcr:created")) {
            gen.writeStringField("jcr:created", node.getProperty("jcr:created").
                    getString());
        }
        gen.writeFieldName("mixinNodeTypes");
        gen.writeStartArray();
        for (NodeType nodeType : node.getMixinNodeTypes()) {
            gen.writeString(nodeType.getName());
        }
        gen.writeEndArray();
        gen.writeEndObject();
    }

    private void serializeProperties() throws IOException, RepositoryException {
        gen.writeObjectFieldStart("properties");
        PropertyIterator properties = node.getProperties();
        while (properties.hasNext()) {
            Property prop = properties.nextProperty();
            String propertyName = prop.getName();
            if (includePropertyInProperties(propertyName)) {
                int type = prop.getType();
                if (type == PropertyType.STRING) {
                    gen.writeStringField(prop.getName(), prop.getString());
                } else if (type == PropertyType.DOUBLE) {
                    gen.writeNumberField(propertyName, prop.getDouble());
                } else if (type == PropertyType.LONG) {
                    gen.writeNumberField(propertyName, prop.getLong());
                } else if (type == PropertyType.BOOLEAN) {
                    gen.writeBooleanField(propertyName, prop.getBoolean());
                } else if (type == PropertyType.DATE) {
                    gen.writeStringField(propertyName, prop.getString());
                } else if (type == PropertyType.BINARY) {
                    gen.writeStringField(propertyName, "Binary Data Type not supported");
                } else if (!prop.getDefinition().isMultiple()) {
                    gen.writeStringField(propertyName, prop.getString());
                }
            }
        }
        gen.writeEndObject();
    }

    private boolean includePropertyInProperties(String propertyName) {
        boolean includeProperty = true;
        for (String property : META_JCR_PROPERTIES) {
            if (property.equals(propertyName)) {
                includeProperty = false;
                break;
            }
        }
        return includeProperty;
    }

    private void serializeChildNodes() throws RepositoryException, IOException {
        gen.writeObjectFieldStart("nodes");
        NodeIterator nodes = node.getNodes();
        while (nodes.hasNext()) {
            Node child = nodes.nextNode();
            if (!child.getPrimaryNodeType().equals(recursivePrimaryNodeType(node))) {
                gen.writeObjectFieldStart(child.getName());
                NodeToJsonSerializer serializer = new NodeToJsonSerializer(gen, child);
                serializer.setPrimaryNodeType(this.primaryNodeType);
                serializer.buildJson();
                gen.writeEndObject();
            }
        }
        gen.writeEndObject();
    }

    private NodeType recursivePrimaryNodeType(Node node) throws RepositoryException {
        NodeType nodeTypeToUse = node.getPrimaryNodeType();
        if (this.primaryNodeType != null) {
            nodeTypeToUse = this.primaryNodeType;
        }
        return nodeTypeToUse;
    }

}
