package com.dievision.sinicum.server.jcr.templating;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.dievision.sinicum.server.mgnlAdapters.MgnlContextAdapter;

public class AreaInitializer {
    @JsonProperty
    private final String workspace;
    @JsonProperty
    private final String baseNodeUuid;
    @JsonProperty
    private final String areaName;
    @JsonProperty
    private final List<String> availableComponents;
    @JsonProperty
    private final boolean areaCreated;
    private Node areaNode;
    private Session session;
    private static final Logger logger = LoggerFactory.getLogger(AreaInitializer.class);

    public AreaInitializer(String workspace, String baseNodeUuid, String areaName) {
        this.workspace = workspace;
        this.baseNodeUuid = baseNodeUuid;
        this.areaName = areaName;
        this.areaNode = getAreaNode();
        this.areaCreated = lookupOrCreateArea();
        this.availableComponents = lookupAvailableComponents();
    }

    protected List<String> lookupAvailableComponents() {
        List<String> componentNames = new ArrayList<String>();
        try {
            if (this.areaNode != null) {
                if (this.areaNode.hasNode("availableComponents")) {
                    NodeIterator components = this.areaNode.getNode("availableComponents")
                            .getNodes();
                    while (components.hasNext()) {
                        Node component = components.nextNode();
                        if (component.hasProperty("id")) {
                            componentNames.add(component.getProperty("id").getString());
                        }
                    }
                }
            }
        } catch (RepositoryException e) {
            logger.error("" + e.toString());
        }
        return componentNames;
    }

    private boolean lookupOrCreateArea() {
        boolean areaCreated = false;
        try {
            Node baseNode = null;
            try {
                baseNode = getSession().getNodeByUUID(baseNodeUuid);
            } catch (ItemNotFoundException e) {
                // nothing
            }
            if (baseNode != null) {
                Node area = null;
                try {
                    area = baseNode.getNode(areaName);
                } catch (PathNotFoundException e) {
                    // nothing
                }
                if (area == null && areaNode != null) {
                    areaCreated = createNewArea(baseNode);
                }
            }
        } catch (RepositoryException e) {
            logger.error("Error looking up area node: " + e.toString(), e);
        }
        return areaCreated;
    }

    protected Node getAreaNode() {
        Node areaNode = null;
        Node pageNode = getPageOrComponentNode();
        String template = null;
        try {
            template = getTemplateNameForNode(pageNode);
        } catch (RepositoryException e) {
            logger.error("Error finding template name: " + e.toString());
        }
        if (template != null) {
            ComponentId componentId = new ComponentId(template);
            try {
                Session configSession = MgnlContextAdapter.getJcrSession("config");
                String stmt = "/jcr:root/modules//templates/"
                        + componentId.parentPathElement() + "/" + componentId.getPath() + "/areas"
                        + "/" + areaName;
                Query query = configSession.getWorkspace().getQueryManager()
                        .createQuery(stmt, "xpath");
                NodeIterator iter = query.execute().getNodes();
                if (iter.hasNext()) {
                    areaNode = iter.nextNode();
                }
            } catch (RepositoryException e) {
                logger.error("Error finding the area node: " + e.toString());
            }
        }
        return areaNode;
    }

    protected Node getPageOrComponentNode() {
        Node node = null;
        try {
            Node rootNode = getSession().getRootNode();
            try {
                Node pageNode = getSession().getNodeByUUID(baseNodeUuid);
                while (!isPageOrComponent(pageNode) && !pageNode.equals(rootNode)) {
                    pageNode = pageNode.getParent();
                }
                if (isPageOrComponent(pageNode)) {
                    node = pageNode;
                }
            } catch (ItemNotFoundException e) {
                // nothing
            }
        } catch (RepositoryException e) {
            logger.error("Error finding the node for area " + areaName + ": " + e.toString());
        }
        return node;
    }

    private boolean isPageOrComponent(Node componentNode) throws RepositoryException {
        return "mgnl:page".equals(componentNode.getPrimaryNodeType().getName())
                || "mgnl:component".equals(componentNode.getPrimaryNodeType().getName());
    }

    private boolean createNewArea(Node baseNode) {
        boolean success = false;
        try {
            baseNode.addNode(areaName, "mgnl:area");
            getSession().save();
            success = true;
        } catch (RepositoryException e) {
            logger.error("Error creating area node: " + e.toString(), e);
        }
        return success;
    }

    private Session getSession() throws RepositoryException {
        if (session == null) {
            session = MgnlContextAdapter.getJcrSession(workspace);
        }
        return session;
    }

    private String getTemplateNameForNode(Node node) throws RepositoryException {
        String templateName = null;
        if (node.isNodeType("mgnl:renderable")) {
            templateName = node.getProperty("mgnl:template").getString();
        } else if (node.hasNode("MetaData")) {
            Node metaData = node.getNode("MetaData");
            if (metaData.hasProperty("mgnl:template")) {
                templateName = metaData.getProperty("mgnl:template").getString();
            }
        }
        return templateName;
    }
}
