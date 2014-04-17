package com.dievision.sinicum.server.resources;

import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.representation.Form;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TemplatingResourceTest extends JerseyJackrabbitTest {
    private Node page;
    private static final Logger logger = LoggerFactory.getLogger(TemplatingResourceTest.class);

    @Test
    public void testComponentsForPage() {
        WebResource ws = resource().path(
                "/_templating/components/myModule/homepage/main")
                .queryParam("pretty", "true");
        ClientResponse response = ws.accept(MediaType.APPLICATION_JSON_TYPE)
                .get(ClientResponse.class);
        assertEquals(200, response.getStatus());
        Map map = response.getEntity(Map.class);
        assertEquals("myModule:components/teaser", ((List) map.get("components")).get(0));
    }

    @Test
    public void testDialogsForComponent() {
        WebResource ws = resource().path(
                "/_templating/dialogs/page/myModule/homepage")
                .queryParam("pretty", "true");
        ClientResponse response = ws.accept(MediaType.APPLICATION_JSON_TYPE)
                .get(ClientResponse.class);
        assertEquals(200, response.getStatus());
        Map map = response.getEntity(Map.class);
        assertEquals("myModule:pageProperties", map.get("dialog"));
    }

    @Test
    public void testAreaInitializer() throws RepositoryException {
        try {
            WebResource ws = resource().path(
                    "/_templating/areas/initialize")
                    .queryParam("pretty", "true");

            MultivaluedMap<String, String> formData = new Form();
            formData.add("workspace", "website");
            formData.add("baseNodeUuid", page.getUUID());
            formData.add("areaName", "main");

            ClientResponse response = ws.accept(MediaType.APPLICATION_JSON_TYPE)
                    .post(ClientResponse.class, formData);
            assertEquals(200, response.getStatus());
            Map map = response.getEntity(Map.class);
            assertTrue((Boolean) map.get("areaCreated"));
        } catch (Exception e) {
            logger.error(e.toString(), e);
        }
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
     * @throws javax.jcr.RepositoryException
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
        Node mainArea = areas.addNode("main", "mgnl:contentNode");
        Node availableComponents = mainArea.addNode("availableComponents", "mgnl:contentNode");
        Node teaser = availableComponents.addNode("teaser", "mgnl:contentNode");
        teaser.setProperty("id", "myModule:components/teaser");

        Node components = templates.addNode("components", "mgnl:content");
        Node pageProperties = components.addNode("pageProperties", "mgnl:contentNode");
        Node teaserComponent = components.addNode("teaser", "mgnl:contentNode");

        // areas for the component
        Node teaserAreas = teaserComponent.addNode("areas", "mgnl:contentNode");
        Node subArea = teaserAreas.addNode("subArea", "mgnl:contentNode");
        Node subAreaComponents = subArea.addNode("availableComponents", "mgnl:contentNode");
        Node subTeaserRef = subAreaComponents.addNode("subTeaser", "mgnl:contentNode");
        subTeaserRef.setProperty("id", "myModule:components/subTeaser");

        // subTeaser component definition
        components.addNode("subTeaser", "mgnl:contentNode");

        rootNode.save();
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
        rootNode.save();
    }
}
