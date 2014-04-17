package com.dievision.sinicum.server.jcr.templating;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.dievision.sinicum.server.mgnlAdapters.MgnlContextAdapter;

public class DialogResolver {
    private final String componentType;
    private String componentPath = null;
    @JsonProperty
    private final String moduleName;
    @JsonProperty
    private final String componentName;
    @JsonProperty
    private final String dialog;

    private static final String PAGE_TYPE = "page";
    private static final String PAGE_PATH = "pages";
    private static final String COMPONENT_TYPE = "component";
    private static final String COMPONENT_PATH = "components";
    private static final Logger logger = LoggerFactory.getLogger(DialogResolver.class);

    public DialogResolver(String componentType, String moduleName, String componentName) {
        this.componentType = componentType;
        this.moduleName = moduleName;
        this.componentName = componentName;
        this.dialog = findDialog();
    }

    private String findDialog() {
        String result = null;
        try {
            Session session = MgnlContextAdapter.getJcrSession("config");
            QueryManager queryManager = session.getWorkspace().getQueryManager();

            String stmt = "/jcr:root/modules/" + moduleName + "/templates/" + getComponentPath()
                    + "/" + componentName;
            Query query = queryManager.createQuery(stmt, Query.XPATH);
            QueryResult queryResult = query.execute();
            NodeIterator iter = queryResult.getNodes();
            if (iter.hasNext()) {
                Node node = iter.nextNode();
                if (node.hasProperty("dialog")) {
                    result = node.getProperty("dialog").getString();
                }
            }
        } catch (RepositoryException e) {
            logger.error("" + e.toString());
        }
        return result;
    }

    public String getComponentPath() {
        if (componentPath == null) {
            if (PAGE_TYPE.equals(componentType)) {
                componentPath = PAGE_PATH;
            } else if (COMPONENT_TYPE.equals(componentType)) {
                componentPath = COMPONENT_PATH;
            }
        }
        return componentPath;
    }
}
