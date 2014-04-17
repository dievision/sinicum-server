package com.dievision.sinicum.server.jcr.templating;

import java.util.ArrayList;
import java.util.List;

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

public class PageAreaResolver {
    @JsonProperty
    private final String moduleName;
    @JsonProperty
    private final String pageName;
    @JsonProperty
    private final String areaName;
    @JsonProperty
    private final List<String> components;
    private static final Logger logger = LoggerFactory.getLogger(PageAreaResolver.class);

    public PageAreaResolver(String moduleName, String pageName, String areaName) {
        this.moduleName = moduleName;
        this.pageName = pageName;
        this.areaName = areaName;
        this.components = findComponents();
    }

    public List<String> findComponents() {
        List<String> result = new ArrayList<String>();
        try {
            Session session = MgnlContextAdapter.getJcrSession("config");
            QueryManager queryManager = session.getWorkspace().getQueryManager();
            String stmt = "/jcr:root/modules/" + moduleName + "/templates/pages/" + pageName
                    + "/areas/" + areaName + "/availableComponents//*[@id]";
            Query query = queryManager.createQuery(stmt, Query.XPATH);
            QueryResult queryResult = query.execute();
            NodeIterator iter = queryResult.getNodes();
            while (iter.hasNext()) {
                Node node = iter.nextNode();
                if (node.hasProperty("id")) {
                    result.add(node.getProperty("id").getString());
                }
            }
        } catch (RepositoryException e) {
            logger.error("" + e.toString());
        }
        return result;
    }

    public List<String> getComponents() {
        return components;
    }
}
