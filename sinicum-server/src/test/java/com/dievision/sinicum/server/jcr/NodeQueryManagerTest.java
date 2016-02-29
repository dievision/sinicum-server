package com.dievision.sinicum.server.jcr;

import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.query.Query;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dievision.sinicum.server.JackrabbitTest45;

import static org.junit.Assert.assertEquals;

public class NodeQueryManagerTest extends JackrabbitTest45 {
    private static final String QUERY = "/jcr:root//*[@jcr:primaryType = 'mgnl:contentNode'] "
            + "order by @jcr:path";
    private static final Logger logger = LoggerFactory.getLogger(NodeQueryManagerTest.class);

    @Before
    public void setUp() throws Exception {
        importFromExportXml(getJcrSession("config").getRootNode(), "/fixtures/templates.xml");
    }

    /* Inconsistent results
    @Test
    public void testExecuteQuery() throws RepositoryException {
        List<NodeApiWrapper> allNodes = getAllNodes();
        assertEquals(203, allNodes.size());
    }
    */

    @Test
    public void testLimit() throws RepositoryException {
        if (isVersion2()) {
            List<NodeApiWrapper> allNodes = getAllNodes();
            NodeQueryManager nodeQueryManager = new NodeQueryManager("config", QUERY, Query.XPATH,
                    2);
            List<NodeApiWrapper> result = nodeQueryManager.executeQuery();
            assertEquals(2, result.size());
            assertEquals(allNodes.get(0).getNode().getIdentifier(),
                    result.get(0).getNode().getIdentifier());
        }
    }

    @Test
    public void testOffset() throws RepositoryException {
        if (isVersion2()) {
            List<NodeApiWrapper> allNodes = getAllNodes();
            NodeQueryManager nodeQueryManager = new NodeQueryManager("config", QUERY, Query.XPATH,
                    0, 1);
            List<NodeApiWrapper> result = nodeQueryManager.executeQuery();
            assertEquals(allNodes.size() - 1, result.size());
            assertEquals(allNodes.get(1).getNode().getIdentifier(),
                    result.get(0).getNode().getIdentifier());
        }
    }

    @Test
    public void testLimitAndOffset() throws RepositoryException {
        if (isVersion2()) {
            List<NodeApiWrapper> allNodes = getAllNodes();
            NodeQueryManager nodeQueryManager = new NodeQueryManager("config", QUERY, Query.XPATH,
                    2, 2);
            List<NodeApiWrapper> result = nodeQueryManager.executeQuery();
            assertEquals(2, result.size());
            assertEquals(allNodes.get(2).getNode().getIdentifier(),
                    result.get(0).getNode().getIdentifier());
        }
    }

    @Test
    public void testCaseInsensivityXPath() {
        NodeQueryManager nodeQueryManager = new NodeQueryManager("config", QUERY, "xPath",
                2, 2);
        assertEquals(Query.XPATH, nodeQueryManager.getLanguage());

    }

    @Test
    public void testCaseInsensivitySql() {
        NodeQueryManager nodeQueryManager = new NodeQueryManager("config", QUERY, "sQl",
                2, 2);
        assertEquals(Query.SQL, nodeQueryManager.getLanguage());

    }

    @Test
    public void testCaseInsensivityJcrSql2() {
        NodeQueryManager nodeQueryManager = new NodeQueryManager("config", QUERY, "jcR-Sql2",
                2, 2);
        assertEquals(Query.JCR_SQL2, nodeQueryManager.getLanguage());

    }

    @Test
    public void testCaseInsensivityJqm() {
        NodeQueryManager nodeQueryManager = new NodeQueryManager("config", QUERY, "jcR-JqOm",
                2, 2);
        assertEquals(Query.JCR_JQOM, nodeQueryManager.getLanguage());

    }

    @Test
    public void testCaseInsensivityNull() {
        NodeQueryManager nodeQueryManager = new NodeQueryManager("config", QUERY, null,
                2, 2);
        assertEquals(null, nodeQueryManager.getLanguage());

    }

    @Test
    public void testCaseInsensivitySomethingElse() {
        NodeQueryManager nodeQueryManager = new NodeQueryManager("config", QUERY, "someThing",
                2, 2);
        assertEquals("someThing", nodeQueryManager.getLanguage());

    }

    private List<NodeApiWrapper> getAllNodes() throws RepositoryException {
        NodeQueryManager nodeQueryManager = new NodeQueryManager("config", QUERY, Query.XPATH);
        return nodeQueryManager.executeQuery();
    }

}
