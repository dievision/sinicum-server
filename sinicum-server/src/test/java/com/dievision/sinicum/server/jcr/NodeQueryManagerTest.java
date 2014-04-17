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
            assertEquals(allNodes.get(0).getNode().getUUID(), result.get(0).getNode().getUUID());
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
            assertEquals(allNodes.get(1).getNode().getUUID(), result.get(0).getNode().getUUID());
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
            assertEquals(allNodes.get(2).getNode().getUUID(), result.get(0).getNode().getUUID());
        }
    }

    private List<NodeApiWrapper> getAllNodes() throws RepositoryException {
        NodeQueryManager nodeQueryManager = new NodeQueryManager("config", QUERY, Query.XPATH);
        return nodeQueryManager.executeQuery();
    }

}
