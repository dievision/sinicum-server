package com.dievision.sinicum.server.resources;

import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dievision.sinicum.server.resources.providers.SinicumObjectProvider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NavigationResourceTest extends JerseyJackrabbitTest {
    private Node node0;
    private Node node010;
    private static final Logger logger = LoggerFactory.getLogger(NavigationResourceTest.class);

    @Override
    protected Application configure() {
        return new ResourceConfig(
                SinicumObjectProvider.class,
                NavigationResource.class
        );
    }

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
    public void testElementWithProperties() throws RepositoryException {
        List response = sendRequest("/" + node0.getIdentifier());
        Map map = (Map) response.get(0);
        assertEquals("/0/00", map.get("path"));
        assertTrue(map.containsKey("uuid"));

        Map properties = (Map) map.get("properties");
        assertEquals(2, properties.size());
        assertEquals("Title", properties.get("title"));
        assertEquals("Nav Title", properties.get("nav_title"));

        assertFalse(map.containsKey("children"));
    }

    @Test
    public void testParentDirectionResponseFormat() throws RepositoryException {
        List response = sendParentRequest(node010.getPath());
        assertEquals(3, response.size());
        Map element = (Map) response.get(0);
        assertTrue(element.containsKey("path"));
        assertTrue(element.containsKey("uuid"));
        assertTrue(element.containsKey("depth"));
        assertTrue(element.containsKey("properties"));
    }

    private List sendRequest(String nodePath) throws RepositoryException {
        List result = target("/_navigation/children" + nodePath)
                .queryParam("properties", "title;nav_title")
                .queryParam("depth", "1")
                .queryParam("pretty", "true")
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .get(List.class);
        return result;
    }

    private List sendParentRequest(String nodePath) throws RepositoryException {
        List result = target("/_navigation/parents" + nodePath)
                .queryParam("properties", "title;nav_title")
                .queryParam("pretty", "true")
                .request()
                .accept(MediaType.APPLICATION_JSON)
                .get(List.class);
        return result;
    }
}
