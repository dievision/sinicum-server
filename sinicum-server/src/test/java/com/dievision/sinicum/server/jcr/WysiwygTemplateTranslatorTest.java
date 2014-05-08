package com.dievision.sinicum.server.jcr;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dievision.sinicum.server.JackrabbitTest45;

import static org.junit.Assert.assertEquals;

public class WysiwygTemplateTranslatorTest extends JackrabbitTest45 {
    private Node page1;
    private Node page2;
    private Node dmsFile;
    private Node damFile;
    private static final Logger logger =
            LoggerFactory.getLogger(WysiwygTemplateTranslatorTest.class);

    @Before
    public void setUp() throws RepositoryException {
        Session session = getJcrSession("website");
        Node root = session.getRootNode();
        page1 = root.addNode("my-node", "mgnl:content");
        page2 = page1.addNode("subnode", "mgnl:content");
        session.save();
        Session dmsSession = getJcrSession("dms");
        Node dmsRoot = dmsSession.getRootNode();
        dmsFile = dmsRoot.addNode("file", "mgnl:content");
        Session damSession = getJcrSession("dam");
        Node damRoot = damSession.getRootNode();
        damFile = damRoot.addNode("file", "mgnl:content");
    }

    @Test
    public void testUuidBasedLink() throws RepositoryException {
        String source = "Start ${link:{uuid:{" + page1.getUUID() + "},"
                + "repository:{" + page1.getSession().getWorkspace().getName() + "},"
                + "handle:{" + page1.getPath() + "},nodeData:{},extension:{html}}} "
                + "End";
        WysiwygTemplateTranslator translator = new WysiwygTemplateTranslator();
        assertEquals("Start /my-node End", translator.translate(source));
    }

    @Test
    public void testMultiReplacement() throws RepositoryException {
        String source = "Start ${link:{uuid:{" + page1.getUUID() + "},"
                + "repository:{" + page1.getSession().getWorkspace().getName() + "},"
                + "handle:{" + page1.getPath() + "},nodeData:{},extension:{html}}} "
                + "in between "
                + "${link:{uuid:{" + page2.getUUID() + "},"
                + "repository:{" + page2.getSession().getWorkspace().getName() + "},"
                + "handle:{" + page2.getPath() + "},nodeData:{},extension:{html}}} "
                + "End";
        WysiwygTemplateTranslator translator = new WysiwygTemplateTranslator();
        assertEquals("Start /my-node in between /my-node/subnode End",
                translator.translate(source));
    }

    @Test
    public void testDmsLink() throws RepositoryException {
        String source = "Start ${link:{uuid:{" + dmsFile.getUUID() + "},"
                + "repository:{" + dmsFile.getSession().getWorkspace().getName() + "},"
                + "handle:{" + dmsFile.getPath() + "},nodeData:{},extension:{html}}} "
                + "End";
        WysiwygTemplateTranslator translator = new WysiwygTemplateTranslator();
        assertEquals("Start /dmsfiles/default/file End", translator.translate(source));
    }

    @Test
    public void testDamLink() throws RepositoryException {
        String source = "Start ${link:{uuid:{" + damFile.getUUID() + "},"
                + "repository:{" + damFile.getSession().getWorkspace().getName() + "},"
                + "path:{" + damFile.getPath() + "},nodeData:{},extension:{html}}} "
                + "End";
        WysiwygTemplateTranslator translator = new WysiwygTemplateTranslator();
        assertEquals("Start /damfiles/default/file End", translator.translate(source));
    }

}
