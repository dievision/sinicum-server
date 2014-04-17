package com.dievision.sinicum.server.jcr.templating.configuration;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import com.dievision.sinicum.server.jcr.PropertyToJsonTypeTranslator;
import com.dievision.sinicum.server.mgnlAdapters.MgnlContextAdapter;

/**
 * Experimental Prototype.
 */
public class ConfigNodeSerializationWrapper {
    private final Node node;

    private static final YAMLFactory YAML_FACTORY = new YAMLFactory();
    private static final Logger logger =
            LoggerFactory.getLogger(ConfigNodeSerializationWrapper.class);

    public ConfigNodeSerializationWrapper(Node node) {
        this.node = node;
    }

    public Map<String, Object> getTemplates() throws RepositoryException {
        Map<String, Object> results = new LinkedHashMap<String, Object>();
        buildHierarchy(node, results);
        return results;
    }

    private void buildHierarchy(Node node, Map<String, Object> map) throws RepositoryException {
        getProperties(map, node);
        NodeIterator iterator = node.getNodes();
        while (iterator.hasNext()) {
            Node child = iterator.nextNode();
            if ("MetaData".equals(child.getName())) {
                continue;
            }
            Map<String, Object> childMap = new LinkedHashMap<String, Object>();
            getNodes(childMap, child);
            map.put(nodeSerializationName(child), childMap);
        }
    }

    private String nodeSerializationName(Node child) throws RepositoryException {
        String prefix = "/";
        if ("mgnl:content".equals(child.getPrimaryNodeType().getName())) {
            prefix = "//";
        }
        return prefix + child.getName();
    }

    private void getProperties(Map<String, Object> map, Node node)
        throws RepositoryException {
        PropertyToJsonTypeTranslator translator = new PropertyToJsonTypeTranslator();
        PropertyIterator iterator = node.getProperties();
        while (iterator.hasNext()) {
            Property property = iterator.nextProperty();
            if (property.getName().indexOf("jcr:") == 0) {
                continue;
            }
            map.put(property.getName(), translator.resolvePropertyToJsonType(property));
        }
    }

    private void getNodes(Map<String, Object> map, Node node) throws RepositoryException {
        NodeIterator iterator = node.getNodes();
        while (iterator.hasNext()) {
            Node child = iterator.nextNode();
            buildHierarchy(node, map);
        }
    }

    public static String serializeNodes() {
        ObjectMapper mapper = new ObjectMapper(YAML_FACTORY);
        String result = "";
        try {
            Node root = MgnlContextAdapter.getJcrSession("config").getRootNode();
            Node moduleBase = root.getNode("modules/myModule/templates");
            logger.debug(moduleBase.getPath());
            result = mapper.writeValueAsString(new ConfigNodeSerializationWrapper(moduleBase));
        } catch (JsonProcessingException e) {
            logger.error("" + e.toString());
        } catch (RepositoryException e) {
            logger.error("" + e.toString());
        }
        return result;
    }

}
