package com.dievision.sinicum.server.jcr;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dievision.sinicum.server.mgnlAdapters.MgnlContextAdapter;

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
        this.query = query;
        this.language = language;
        this.limit = limit;
        this.offset = offset;
    }

    public List<NodeApiWrapper> executeQuery() throws RepositoryException {
        Session session = MgnlContextAdapter.getJcrSession(workspace);
        QueryManager queryManager = session.getWorkspace().getQueryManager();
        Query qry = queryManager.createQuery(query, language);

        if (limitAndOffsetSupported()) {
            try {
                if (limit > 0) {
                    Method m = qry.getClass().getDeclaredMethod("setLimit", Long.TYPE);
                    m.invoke(qry, limit);
                }
                if (offset > 0) {
                    Method m = qry.getClass().getDeclaredMethod("setOffset", Long.TYPE);
                    m.invoke(qry, offset);
                }
            } catch (Exception e) {
                logger.error("Could not set limit or offset for query: " + e.toString());
            }
        }
        NodeIterator result = qry.execute().getNodes();
        List<NodeApiWrapper> nodes = new ArrayList<NodeApiWrapper>();
        while (result.hasNext()) {
            Node node = result.nextNode();
            nodes.add(new NodeApiWrapper4(node, node.getPrimaryNodeType()));
        }
        return nodes;
    }

    private boolean limitAndOffsetSupported() {
        try {
            boolean checkMethod = false;
            BeanInfo info = Introspector.getBeanInfo(Query.class);
            for (MethodDescriptor md : info.getMethodDescriptors()) {
                if ("setLimit".equals(md.getName())) {
                    checkMethod = true;
                    break;
                }
            }
            return checkMethod;
        } catch (IntrospectionException e) {
            return false;
        }
    }
}
