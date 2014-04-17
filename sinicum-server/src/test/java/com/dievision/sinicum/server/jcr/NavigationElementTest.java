package com.dievision.sinicum.server.jcr;

import java.util.Arrays;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dievision.sinicum.server.JackrabbitTest45;

import static org.junit.Assert.*;

public class NavigationElementTest extends JackrabbitTest45 {
    Node node;
    private static final Logger logger = LoggerFactory.getLogger(NavigationElementTest.class);

    @Before
    public void setUpRepository() throws RepositoryException {
        Session session = getJcrSession("website");
        node = session.getRootNode().addNode("0", "mgnl:page");
        node.setProperty("title", "Title");
        node.setProperty("navigation_title", "Navigation Title");
        Node navHidden = node.addNode("nav_hidden", "mgnl:contentNode");
        navHidden.setProperty("0", true);
        session.save();
    }

    @Test
    public void testReturnsCorrectProperties() throws RepositoryException {
        NavigationElement element = new NavigationElement(node, Arrays.asList("title"));
        assertEquals("Title", element.getProperties().get("title"));
        assertFalse(element.getProperties().containsKey("navigation_title"));
        assertEquals(1, element.getDepth());
    }

    @Test
    public void testHandlesNestedProperties() throws RepositoryException {
        NavigationElement element = new NavigationElement(node, Arrays.asList("nav_hidden.0"));
        assertTrue((Boolean) element.getProperties().get("nav_hidden.0"));
    }
}
