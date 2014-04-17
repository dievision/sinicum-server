package com.dievision.sinicum.server.jcr;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;

public class NavigationElement {
    private final Node node;
    private List<String> properties;
    private List<NavigationElement> children;
    private PropertyToJsonTypeTranslator translator = new PropertyToJsonTypeTranslator();
    private static final Logger logger = LoggerFactory.getLogger(NavigationElement.class);

    public NavigationElement(Node node, List<String> properties) {
        this.node = node;
        this.properties = properties;
    }

    public String getPath() {
        String result = null;
        try {
            result = node.getPath();
        } catch (RepositoryException e) {
            logger.error("Could not resolve path: " + e.toString());
        }
        return result;
    }

    public String getUuid() {
        String result = null;
        try {
            result = node.getUUID();
        } catch (RepositoryException e) {
            logger.error("Could not resolve UUID: " + e.toString());
        }
        return result;
    }

    public int getDepth() {
        int depth = 0;
        try {
            depth = node.getDepth();
        } catch (RepositoryException e) {
            logger.error("Could not relsove navigation depth: " + e.toString());
        }
        return depth;
    }

    public Map<String, Object> getProperties() {
        Map<String, Object> props = new HashMap<String, Object>();
        try {
            for (String propName : properties) {
                Object value = fetchProperty(node, propName);
                if (value != null) {
                    props.put(propName, value);
                }
            }
        } catch (RepositoryException e) {
            logger.error("Could not resolve properties: " + e.toString());
        }
        return props;
    }

    private Object fetchProperty(Node node, String propertyName)
        throws RepositoryException {
        Object result = null;
        if (propertyName.contains(".")) {
            String firstPart = propertyName.substring(0, propertyName.indexOf("."));
            String lastPart = propertyName.substring(propertyName.indexOf(".") + 1,
                    propertyName.length());
            if (node.hasNode(firstPart)) {
                result = fetchProperty(node.getNode(firstPart), lastPart);
            }
        } else if (node != null && node.hasProperty(propertyName)) {
            result = translator.resolvePropertyToJsonType(node.getProperty(propertyName));
            return result;
        }
        return result;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public List<NavigationElement> getChildren() {
        if (this.children != null && this.children.size() == 0) {
            return null;
        } else {
            return this.children;
        }
    }

    protected void setChildren(List<NavigationElement> children) {
        this.children = children;
    }
}
