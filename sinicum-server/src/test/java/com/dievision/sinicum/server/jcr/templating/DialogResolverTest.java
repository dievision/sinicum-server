package com.dievision.sinicum.server.jcr.templating;

import java.io.IOException;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dievision.sinicum.server.JackrabbitTest45;

import static com.dievision.sinicum.server.JsonResourceHelper.resourceObjectToJsonMap;
import static org.junit.Assert.assertEquals;

public class DialogResolverTest extends JackrabbitTest45 {
    private static final Logger logger = LoggerFactory.getLogger(DialogResolverTest.class);

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

        Node compontents = templates.addNode("components", "mgnl:content");
        Node pageProperties = modules.addNode("pageProperties", "mgnl:content");

        rootNode.save();
    }

    @Test
    public void testFindRightDialog() throws IOException {
        DialogResolver resolver = new DialogResolver("page", "myModule", "homepage");
        Map map = resourceObjectToJsonMap(resolver);
        assertEquals("myModule:pageProperties", map.get("dialog"));
    }

}
