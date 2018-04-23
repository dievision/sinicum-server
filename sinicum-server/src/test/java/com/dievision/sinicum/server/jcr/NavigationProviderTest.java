package com.dievision.sinicum.server.jcr;

import java.util.Arrays;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dievision.sinicum.server.JackrabbitTest45;

import static org.junit.Assert.*;

public class NavigationProviderTest extends JackrabbitTest45 {
    private Node node0;
    private static final Logger logger = LoggerFactory.getLogger(NavigationProviderTest.class);

    @Before
    public void setUpRepository() throws RepositoryException {
        Session session = getJcrSession("website");
        node0 = session.getRootNode().addNode("0", "mgnl:page");
        Node node00 = node0.addNode("00", "mgnl:page");
        node00.setProperty("title", "Title");
        node00.setProperty("navigation_title", "Navigation Title");
        node00.setProperty("nav_hidden", false);
        Node node01 = node0.addNode("01", "mgnl:page");
        Node area = node0.addNode("00area", "mgnl:area");
        Node node000 = node00.addNode("000", "mgnl:page");
        Node node001 = node00.addNode("001", "mgnl:page");
        Node node010 = node01.addNode("010", "mgnl:page");
        Node node011 = node01.addNode("011", "mgnl:page");
        session.save();
    }

    @Test
    public void testOneLevelNavigationUuidBase() throws RepositoryException {
        NavigationProvider provider = new NavigationProvider(node0.getIdentifier(),
                Arrays.asList("title"), 1);
        List<NavigationElement> elements = provider.getNavigationElements();
        assertEquals(2, elements.size());
    }

    @Test
    public void testOneLevelNavigationPathBase() throws RepositoryException {
        NavigationProvider provider = new NavigationProvider(node0.getPath().substring(1),
                Arrays.asList("title"), 1);
        List<NavigationElement> elements = provider.getNavigationElements();
        assertEquals(2, elements.size());
    }

    @Test
    public void testReturnsCorrectNodes() throws RepositoryException {
        NavigationProvider provider = new NavigationProvider(node0.getIdentifier(),
                Arrays.asList("title"), 1);
        List<NavigationElement> elements = provider.getNavigationElements();
        assertEquals("/0/00", elements.get(0).getPath());
        assertEquals("/0/01", elements.get(1).getPath());
    }

    @Test
    public void testReturnsNoChildNodes() throws RepositoryException {
        NavigationProvider provider = new NavigationProvider(node0.getIdentifier(),
                Arrays.asList("title"), 1);
        List<NavigationElement> elements = provider.getNavigationElements();
        assertNull(elements.get(0).getChildren());
    }

    @Test
    public void testReturnsMultipleLevels() throws RepositoryException {
        NavigationProvider provider = new NavigationProvider(node0.getIdentifier(),
                Arrays.asList("title"), 2);
        List<NavigationElement> elements = provider.getNavigationElements();
        assertEquals("/0/00", elements.get(0).getPath());
        List<NavigationElement> children = elements.get(0).getChildren();
        assertEquals(2, children.size());
        assertEquals("/0/00/000", children.get(0).getPath());
        assertEquals("/0/00/001", children.get(1).getPath());
    }
}
