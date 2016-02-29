package com.dievision.sinicum.server.resources;

import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dievision.sinicum.server.resources.providers.SinicumObjectProvider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class NodeResourceTest extends JerseyJackrabbitTest {
    private static final Logger logger = LoggerFactory.getLogger(NodeResourceTest.class);

    @Override
    protected Application configure() {
        return new ResourceConfig(
                SinicumObjectProvider.class,
                NodeResource.class
        );
    }

    @Before
    public void setUpChild() throws RepositoryException {
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
        Response response = executeRequest();
        assertEquals(200, response.getStatus());
        List result = response.readEntity(List.class);
        assertEquals(2, result.size());
    }

    @Test
    public void testQueryResultFormat() {
        Response response = executeRequest();
        List result = response.readEntity(List.class);
        Map map = (Map) result.get(0);
        assertTrue(map.containsKey("meta"));
        Map meta = (Map) map.get("meta");
        assertEquals("/path/subpage1", (meta.get("path")));
        assertTrue(map.containsKey("properties"));
        assertTrue(map.containsKey("nodes"));
    }

    private Response executeRequest() {
        /*
        WebResource ws = resource().path(
                "/website/_query")
                .queryParam("query", "/jcr:root/path//element(*, mgnl:page)")
                .queryParam("language", "xpath")
                .queryParam("pretty", "true");
        return ws.accept(MediaType.APPLICATION_JSON_TYPE)
                .get(ClientResponse.class);
                */
        Response response = target()
                .path("/website/_query")
                .queryParam("query", "/jcr:root/path//element(*, mgnl:page)")
                .queryParam("language", "xpath")
                .queryParam("pretty", "true")
                .request(MediaType.APPLICATION_JSON)
                .get();
        return response;
    }

}
