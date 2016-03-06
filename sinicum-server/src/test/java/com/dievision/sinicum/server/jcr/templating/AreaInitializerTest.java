package com.dievision.sinicum.server.jcr.templating;

import java.util.ArrayList;
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

public class AreaInitializerTest extends JackrabbitTest45 {
    private Node page;
    private Node teaser;
    private Node mainArea;
    private Node subArea;
    private static final Logger logger = LoggerFactory.getLogger(AreaInitializerTest.class);

    @Test
    public void testFindPageToNode() throws RepositoryException {
        AreaInitializer initializer = new AreaInitializer("website", page.getIdentifier(), "main");
        assertEquals(mainArea.getIdentifier(), initializer.getAreaNode().getIdentifier());
    }

    @Test
    public void testFindAvailableComponents() throws RepositoryException {
        AreaInitializer initializer = new AreaInitializer("website", page.getIdentifier(), "main");
        List<String> results = new ArrayList<String>();
        results.add("myModule:components/teaser");
        assertEquals(results, initializer.lookupAvailableComponents());
    }

    @Test
    public void testAreaCreated() throws RepositoryException {
        Session session = getJcrSession("website");
        assertFalse(session.getRootNode().hasNode("page/main"));
        AreaInitializer initializer = new AreaInitializer("website", page.getIdentifier(),
                "main");
        Node subArea = session.getRootNode().getNode("page/main");
        assertEquals("mgnl:area", subArea.getPrimaryNodeType().getName());
    }

    @Test
    public void testFindSubArea() throws RepositoryException {
        setUpArea();
        AreaInitializer initializer = new AreaInitializer("website", teaser.getIdentifier(),
                "subArea");
        assertEquals(subArea.getPath(), initializer.getAreaNode().getPath());
    }

    @Test
    public void testFindAvailableComponentsForSubArea() throws RepositoryException {
        setUpArea();
        AreaInitializer initializer = new AreaInitializer("website", teaser.getIdentifier(),
                "subArea");
        List<String> results = new ArrayList<String>();
        results.add("myModule:components/subTeaser");
        assertEquals(results, initializer.lookupAvailableComponents());
    }

    @Test
    public void testSubAreaCreated() throws RepositoryException {
        setUpArea();
        Session session = getJcrSession("website");
        assertFalse(session.getRootNode().hasNode("page/main/0/subArea"));
        AreaInitializer initializer = new AreaInitializer("website", teaser.getIdentifier(),
                "subArea");
        Node subArea = session.getRootNode().getNode("page/main/0/subArea");
        assertEquals("mgnl:area", subArea.getPrimaryNodeType().getName());
    }

    /**
     * Sets up the structure for the configuration.
     *
     * /modules
     *   /myModule
     *     /templates
     *       /pages
     *         o homepage
     *           x dialog
     *           o areas
     *             o main
     *               o availableComponents
     *                 x id=myModule:components/teaser
     *       /components
     *         o pageProperties
     *         o teaser
     *           o areas
     *             o subArea
     *               o availableComponents
     *                 x id=myModule:components/subTeaser
     *         o subTeaser
     *
     * @throws RepositoryException
     */
    @Before
    public void setUpDialogs() throws RepositoryException {
        Session session = getJcrSession("config");
        Node rootNode = session.getRootNode();
        Node modules = rootNode.addNode("modules", "mgnl:content");
        Node myModule = modules.addNode("myModule", "mgnl:content");
        Node templates = myModule.addNode("templates", "mgnl:content");
        Node pages = templates.addNode("pages", "mgnl:content");
        Node homepage = pages.addNode("homepage", "mgnl:contentNode");
        homepage.setProperty("dialog", "myModule:pageProperties");

        Node areas = homepage.addNode("areas", "mgnl:contentNode");
        mainArea = areas.addNode("main", "mgnl:contentNode");
        Node availableComponents = mainArea.addNode("availableComponents", "mgnl:contentNode");
        Node teaser = availableComponents.addNode("teaser", "mgnl:contentNode");
        teaser.setProperty("id", "myModule:components/teaser");

        Node components = templates.addNode("components", "mgnl:content");
        Node pageProperties = components.addNode("pageProperties", "mgnl:contentNode");
        Node teaserComponent = components.addNode("teaser", "mgnl:contentNode");

        // areas for the component
        Node teaserAreas = teaserComponent.addNode("areas", "mgnl:contentNode");
        subArea = teaserAreas.addNode("subArea", "mgnl:contentNode");
        Node subAreaComponents = subArea.addNode("availableComponents", "mgnl:contentNode");
        Node subTeaserRef = subAreaComponents.addNode("subTeaser", "mgnl:contentNode");
        subTeaserRef.setProperty("id", "myModule:components/subTeaser");

        // subTeaser component definition
        components.addNode("subTeaser", "mgnl:contentNode");

        session.save();
    }

    /**
     * Sets up the structure for a blank page.
     *
     * /page
     */
    @Before
    public void setUpWebsite() throws RepositoryException {
        Session session = getJcrSession("website");
        Node rootNode = session.getRootNode();
        page = rootNode.addNode("page", "mgnl:page");
        Node metaData = page.getNode("MetaData");
        metaData.setProperty("mgnl:template", "myModule:pages/homepage");
        session.save();
    }

    /**
     * Sets up the stucture for a page with a component.
     *
     * /page (from setUpWebsite)
     *   /main (area)
     *     /0 (teaserComponent)
     */
    private void setUpArea() throws RepositoryException {
        Node area = page.addNode("main", "mgnl:area");
        teaser = area.addNode("0", "mgnl:component");
        teaser.getNode("MetaData").setProperty("mgnl:template", "myModule:components/teaser");
    }
}
