package com.dievision.sinicum.server.resources;

import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.ws.rs.core.MediaType;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NodeResourceTest extends JerseyJackrabbitTest {
    private static final Logger logger = LoggerFactory.getLogger(NodeResourceTest.class);

    @Before
    public void setUp() throws RepositoryException {
        Session session = getJcrSession("website");
        Node root = session.getRootNode();
        Node path1 = root.addNode("path", "mgnl:page");
        Node component = path1.addNode("component", "mgnl:component");
        Node path11 = path1.addNode("subpage1", "mgnl:page");
        Node path12 = path1.addNode("subpage2", "mgnl:page");
        Node path2 = root.addNode("path2", "mgnl:page");
        session.save();
    }

    @Test
    public void testQuery() {
        ClientResponse response = executeRequest();
        assertEquals(200, response.getStatus());
        List result = response.getEntity(List.class);
        assertEquals(2, result.size());
    }

    @Test
    public void testQueryResultFormat() {
        ClientResponse response = executeRequest();
        List result = response.getEntity(List.class);
        Map map = (Map) result.get(0);
        assertTrue(map.containsKey("meta"));
        Map meta = (Map) map.get("meta");
        assertEquals("/path/subpage1", (meta.get("path")));
        assertTrue(map.containsKey("properties"));
        assertTrue(map.containsKey("nodes"));
    }

    private ClientResponse executeRequest() {
        WebResource ws = resource().path(
                "/website/_query")
                .queryParam("query", "/jcr:root/path//element(*, mgnl:page)")
                .queryParam("language", "xpath")
                .queryParam("pretty", "true");
        return ws.accept(MediaType.APPLICATION_JSON_TYPE)
                .get(ClientResponse.class);
    }

}
