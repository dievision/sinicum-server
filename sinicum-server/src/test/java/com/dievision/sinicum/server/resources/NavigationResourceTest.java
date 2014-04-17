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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NavigationResourceTest extends JerseyJackrabbitTest {
    private Node node0;
    private Node node010;
    private static final Logger logger = LoggerFactory.getLogger(NavigationResourceTest.class);

    @Before
    public void setUpRepository() throws RepositoryException {
        Session session = getJcrSession("website");
        node0 = session.getRootNode().addNode("0", "mgnl:page");
        Node node00 = node0.addNode("00", "mgnl:page");
        node00.setProperty("title", "Title");
        node00.setProperty("nav_title", "Nav Title");
        node00.setProperty("some_property", "Prop");
        Node node01 = node0.addNode("01", "mgnl:page");
        Node node000 = node00.addNode("000", "mgnl:page");
        Node node001 = node00.addNode("001", "mgnl:page");
        node010 = node01.addNode("010", "mgnl:page");
        Node node011 = node01.addNode("011", "mgnl:page");
        session.save();
    }

    @Test
    public void testSuccess() throws RepositoryException {
        ClientResponse response = sendRequest("/" + node0.getUUID());
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testSuccessWithPath() throws RepositoryException {
        ClientResponse response = sendRequest(node0.getPath());
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testElementWithProperties() throws RepositoryException {
        ClientResponse response = sendRequest("/" + node0.getUUID());
        List result = response.getEntity(List.class);
        Map map = (Map) result.get(0);
        assertEquals("/0/00", map.get("path"));
        assertTrue(map.containsKey("uuid"));

        Map properties = (Map) map.get("properties");
        assertEquals(2, properties.size());
        assertEquals("Title", properties.get("title"));
        assertEquals("Nav Title", properties.get("nav_title"));

        assertFalse(map.containsKey("children"));
    }

    @Test
    public void testParentDirectionResponse() throws RepositoryException {
        ClientResponse response = sendParentRequest(node010.getPath());
        String res = response.getEntity(String.class);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testParentDirectionResponseFormat() throws RepositoryException {
        ClientResponse response = sendParentRequest(node010.getPath());
        List list = response.getEntity(List.class);
        assertEquals(3, list.size());
        Map element = (Map) list.get(0);
        assertTrue(element.containsKey("path"));
        assertTrue(element.containsKey("uuid"));
        assertTrue(element.containsKey("depth"));
        assertTrue(element.containsKey("properties"));
    }

    private ClientResponse sendRequest(String nodePath) throws RepositoryException {
        WebResource ws = resource().path(
                "/_navigation/children" + nodePath)
                .queryParam("properties", "title;nav_title")
                .queryParam("depth", "1")
                .queryParam("pretty", "true");
        return ws.accept(MediaType.APPLICATION_JSON_TYPE)
                .get(ClientResponse.class);
    }

    private ClientResponse sendParentRequest(String nodePath) throws RepositoryException {
        WebResource ws = resource().path(
                "/_navigation/parents" + nodePath)
                .queryParam("properties", "title;nav_title")
                .queryParam("pretty", "true");
        return ws.accept(MediaType.APPLICATION_JSON_TYPE)
                .get(ClientResponse.class);
    }
}
