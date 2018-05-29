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
import static org.junit.Assert.assertTrue;

public class WysiwygTemplateTranslatorTest extends JackrabbitTest45 {
    private Node page1;
    private Node page2;
    private Node page3;
    private Node page4;
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
        page3 = root.addNode("en-GB", "mgnl:page").addNode("nice-subpage", "mgnl:page");
        page4 = root.addNode("b2b", "mgnl:page").addNode("de-DE", "mgnl:page")
                    .addNode("b2b-subpage", "mgnl:page");
        session.save();
        Session dmsSession = getJcrSession("dms");
        Node dmsRoot = dmsSession.getRootNode();
        dmsFile = dmsRoot.addNode("file", "mgnl:content");
        Session damSession = getJcrSession("dam");
        Node damRoot = damSession.getRootNode();
        damFile = damRoot.addNode("file", "mgnl:content");
        Node damContent = damFile.addNode("jcr:content", "mgnl:resource");
        damContent.setProperty("extension", "pdf");
        damContent.setProperty("size", "12345");

        session = getJcrSession("multisite");
        root = session.getRootNode();
        Node multisiteNode = root.addNode("en-gb", "mgnl:content");
        multisiteNode.setProperty("root_node", "/en-GB");
        root.addNode("b2b", "mgnl:content").setProperty("root_node", "/b2b/de-DE");
        session.save();
    }

    @Test
    public void testUuidBasedLink() throws RepositoryException {
        String source = "Start ${link:{uuid:{" + page1.getIdentifier() + "},"
                + "repository:{" + page1.getSession().getWorkspace().getName() + "},"
                + "handle:{" + page1.getPath() + "},nodeData:{},extension:{html}}} "
                + "End";
        WysiwygTemplateTranslator translator = new WysiwygTemplateTranslator();
        assertEquals("Start /my-node End", translator.translate(source));
    }

    @Test
    public void testMultiReplacement() throws RepositoryException {
        String source = "Start ${link:{uuid:{" + page1.getIdentifier() + "},"
                + "repository:{" + page1.getSession().getWorkspace().getName() + "},"
                + "handle:{" + page1.getPath() + "},nodeData:{},extension:{html}}} "
                + "in between "
                + "${link:{uuid:{" + page2.getIdentifier() + "},"
                + "repository:{" + page2.getSession().getWorkspace().getName() + "},"
                + "handle:{" + page2.getPath() + "},nodeData:{},extension:{html}}} "
                + "End";
        WysiwygTemplateTranslator translator = new WysiwygTemplateTranslator();
        assertEquals("Start /my-node in between /my-node/subnode End",
                translator.translate(source));
    }

    @Test
    public void testDmsLink() throws RepositoryException {
        String source = "Start ${link:{uuid:{" + dmsFile.getIdentifier() + "},"
                + "repository:{" + dmsFile.getSession().getWorkspace().getName() + "},"
                + "handle:{" + dmsFile.getPath() + "},nodeData:{},extension:{html}}} "
                + "End";
        WysiwygTemplateTranslator translator = new WysiwygTemplateTranslator();
        assertEquals("Start /dmsfiles/default/file End", translator.translate(source));
    }

    @Test
    public void testDamLink() throws RepositoryException {
        String source = "Start ${link:{uuid:{" + damFile.getIdentifier() + "},"
                + "repository:{" + damFile.getSession().getWorkspace().getName() + "},"
                + "path:{" + damFile.getPath() + "},nodeData:{},extension:{html}}} "
                + "End";
        WysiwygTemplateTranslator translator = new WysiwygTemplateTranslator();
        assertTrue(translator.translate(source).
                matches("Start /damfiles/default/file-[a-f0-9]{32}.pdf End"));
    }

    @Test
    public void testMultisiteLink() throws RepositoryException {
        assertEquals("/en-GB/nice-subpage", page3.getPath());
        String source = "Start ${link:{uuid:{" + page3.getIdentifier() + "},"
                + "repository:{" + page3.getSession().getWorkspace().getName() + "},"
                + "handle:{" + page3.getPath() + "},nodeData:{},extension:{html}}} "
                + "End";
        WysiwygTemplateTranslator translator = new WysiwygTemplateTranslator();
        assertEquals("Start /nice-subpage End", translator.translate(source));
    }

    @Test
    public void testTwoLevelMultisiteLink() throws RepositoryException {
        assertEquals("/b2b/de-DE/b2b-subpage", page4.getPath());
        String source = "Start ${link:{uuid:{" + page4.getIdentifier() + "},"
                + "repository:{" + page4.getSession().getWorkspace().getName() + "},"
                + "handle:{" + page4.getPath() + "},nodeData:{},extension:{html}}} "
                + "End";
        WysiwygTemplateTranslator translator = new WysiwygTemplateTranslator();
        assertEquals("Start /b2b-subpage End", translator.translate(source));
    }

}
