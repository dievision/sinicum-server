package com.dievision.sinicum.server.jcr;

import com.dievision.sinicum.server.mgnlAdapters.MgnlContextAdapter;
import org.apache.jackrabbit.util.ISO9075;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NodeQueryManager {
    private final String workspace;
    private final String query;
    private final String language;
    private final long limit;
    private final long offset;
    private static final Logger logger = LoggerFactory.getLogger(NodeQueryManager.class);

    public NodeQueryManager(String workspace, String query, String language) {
        this(workspace, query, language, 0, 0);
    }

    public NodeQueryManager(String workspace, String query, String language, long limit) {
        this(workspace, query, language, limit, 0);
    }

    public NodeQueryManager(String workspace, String query, String language,
                            long limit, long offset) {
        this.workspace = workspace;
        this.query = encodeQuery(query, language);
        this.language = convertQueryLanguage(language);
        this.limit = limit;
        this.offset = offset;
    }

    public List<NodeApiWrapper> executeQuery() throws RepositoryException {
        Session session = MgnlContextAdapter.getJcrSession(workspace);
        QueryManager queryManager = session.getWorkspace().getQueryManager();
        Query qry = queryManager.createQuery(query, language);
        if (limit > 0) {
            qry.setLimit(limit);
        }
        if (offset > 0) {
            qry.setOffset(offset);
        }
        NodeIterator result = qry.execute().getNodes();
        List<NodeApiWrapper> nodes = new ArrayList<NodeApiWrapper>();
        while (result.hasNext()) {
            Node node = result.nextNode();
            NodeResolver nodeResolver = new NodeResolver(node);
            nodes.add(nodeResolver.getNode());
        }
        return nodes;
    }

    protected String getLanguage() {
        return language;
    }

    private String encodeQuery(String query, String queryLanguage) {
        if (Query.XPATH.equalsIgnoreCase(queryLanguage)) {
            query = encodeXPathQuery(query);
        }
        return query;
    }

    private String convertQueryLanguage(String queryLanguage) {
        if (Query.XPATH.equalsIgnoreCase(queryLanguage)) {
            return Query.XPATH;
        } else if (Query.SQL.equalsIgnoreCase(queryLanguage)) {
            return Query.SQL;
        } else if (Query.JCR_SQL2.equalsIgnoreCase(queryLanguage)) {
            return Query.JCR_SQL2;
        } else if (Query.JCR_JQOM.equalsIgnoreCase(queryLanguage)) {
            return Query.JCR_JQOM;
        } else {
            return queryLanguage;
        }
    }

    private String encodeXPathQuery(String query) {
        String [] toEncodeNodes = query.split("\\[(.?)\\]|\\[(.*)\\]");
        String [] encodedNodes = new String[toEncodeNodes.length];
        for (int i = 0; i < toEncodeNodes.length; i++) {
            String replaceNode = toEncodeNodes[i];
            Matcher matchNode = Pattern.compile("[0-9]+").matcher(toEncodeNodes[i]);
            while (matchNode.find()) {
                replaceNode =
                        replaceNode.replace(matchNode.group(0), ISO9075.encode(matchNode.group(0)));
                encodedNodes[i] = replaceNode;
            }
        }
        if (encodedNodes[0] != null) {
            for (int i = 0; i < toEncodeNodes.length; i++) {
                String toEncodeNode = toEncodeNodes[i];
                String encodedNode = encodedNodes[i];
                int startIndex, endIndex = 0;
                startIndex = query.indexOf(toEncodeNode);
                endIndex = startIndex + toEncodeNode.length();
                query = query.substring(0, startIndex) + encodedNode + query.substring(endIndex);
            }
        }
        return query;
    }
}
