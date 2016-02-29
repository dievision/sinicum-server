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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ParentNavigationProviderTest extends JackrabbitTest45 {
    private Node node0;
    private Node node01;
    private Node node010;
    private static final Logger logger =
            LoggerFactory.getLogger(ParentNavigationProviderTest.class);

    @Before
    public void setUpRepository() throws RepositoryException {
        Session session = getJcrSession("website");
        node0 = session.getRootNode().addNode("0", "mgnl:page");
        Node node00 = node0.addNode("00", "mgnl:page");
        node01 = node0.addNode("01", "mgnl:page");
        node01.setProperty("title", "Title");
        node01.setProperty("navigation_title", "Navigation Title");
        Node area = node0.addNode("00area", "mgnl:area");
        Node node000 = node00.addNode("000", "mgnl:page");
        Node node001 = node00.addNode("001", "mgnl:page");
        node010 = node01.addNode("010", "mgnl:page");
        Node node011 = node01.addNode("011", "mgnl:page");
        session.save();
    }

    @Test
    public void testCorrectSizeOfElements() throws RepositoryException {
        ParentNavigationProvider provider = new ParentNavigationProvider(node010.getIdentifier(),
                Arrays.asList("title"));
        List<NavigationElement> elements = provider.getNavigationElements();
        assertEquals(3, elements.size());
    }

    @Test
    public void testCorrectElements() throws RepositoryException {
        ParentNavigationProvider provider = new ParentNavigationProvider(node010.getIdentifier(),
                Arrays.asList("title"));
        List<NavigationElement> elements = provider.getNavigationElements();
        assertEquals(node0.getPath(), elements.get(0).getPath());
        assertEquals(node01.getPath(), elements.get(1).getPath());
        assertEquals(node010.getPath(), elements.get(2).getPath());
    }

    @Test
    public void testCorrectProperties() throws RepositoryException {
        ParentNavigationProvider provider = new ParentNavigationProvider(node010.getIdentifier(),
                Arrays.asList("title"));
        List<NavigationElement> elements = provider.getNavigationElements();
        NavigationElement element = elements.get(1);
        assertEquals(2, element.getDepth());
        assertEquals("Title", element.getProperties().get("title"));
        assertFalse(element.getProperties().containsKey("navigation_title"));
    }


}
