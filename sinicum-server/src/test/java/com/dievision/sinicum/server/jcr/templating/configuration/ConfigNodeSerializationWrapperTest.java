package com.dievision.sinicum.server.jcr.templating.configuration;

import java.io.IOException;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dievision.sinicum.server.JackrabbitTest45;

public class ConfigNodeSerializationWrapperTest extends JackrabbitTest45 {
    private static Session session;
    private static final Logger logger =
            LoggerFactory.getLogger(ConfigNodeSerializationWrapperTest.class);

    @Before
    public void importTree() throws RepositoryException, IOException {
        session = getJcrSession("config");
        Node modules = session.getRootNode().addNode("modules", "mgnl:content");
        Node module = modules.addNode("myModule", "mgnl:content");
        importFromExportXml(module, "/fixtures/templates.xml");
    }

    @Test
    public void testSerialization() {
        String yaml = ConfigNodeSerializationWrapper.serializeNodes();
        // logger.debug(yaml);
    }
}
