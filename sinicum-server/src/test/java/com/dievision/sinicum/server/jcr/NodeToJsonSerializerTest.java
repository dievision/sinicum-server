package com.dievision.sinicum.server.jcr;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;

import static junit.framework.Assert.*;

import com.dievision.sinicum.server.JackrabbitTest;

/**
 *
 */
public class NodeToJsonSerializerTest extends JackrabbitTest {
    private static final Logger logger = LoggerFactory.getLogger(NodeToJsonSerializerTest.class);
    OutputStream out;
    JsonGenerator gen;

    @Before
    public void setUp() throws Exception {
        // JSON Generator
        out = new ByteArrayOutputStream();
        JsonFactory json = new JsonFactory();
        gen = json.createJsonGenerator(out, JsonEncoding.UTF8);
        gen.useDefaultPrettyPrinter();
    }

    @Test
    public void testJsonContainsProperties() throws Exception {
        Node node = setUpContentHierarchy();
        List result = toJsonAndBack(node);
        Map first = (Map) result.get(0);
        Map nodes = (Map) first.get("properties");
        assertNotNull(nodes.get("title"));
    }

    @Test
    public void testJsonContainsContentNodes() throws Exception {
        Node node = setUpContentHierarchy();
        List result = toJsonAndBack(node);
        Map first = (Map) result.get(0);
        Map nodes = (Map) first.get("nodes");
        assertNotNull(nodes.get("para"));
    }

    @Test
    public void testJsonContainsNoNodesOfSameType() throws Exception {
        Node node = setUpContentHierarchy();
        List result = toJsonAndBack(node);
        Map first = (Map) result.get(0);
        Map nodes = (Map) first.get("nodes");
        assertNull(nodes.get("company"));
    }

    @Test
    public void testJsonContainsMetadata() throws Exception {
        Node node = setUpContentHierarchy();
        List result = toJsonAndBack(node);
        Map first = (Map) result.get(0);
        assertNotNull(first.get("meta"));
    }

    @Test
    public void testExistenceOfMetadataProperties() throws Exception {
        Node node = setUpContentHierarchy();
        List result = toJsonAndBack(node);
        Map first = (Map) result.get(0);
        Map meta = (Map) first.get("meta");
        Node rootNode = getJcrSession("website").getRootNode();
        NodeIterator iter = rootNode.getNodes();
        assertEquals("website", meta.get("workspace"));
        assertTrue(((String) meta.get("jcr:uuid")).matches(
                "[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}"));
        assertEquals("/de", meta.get("path"));
        assertEquals("mgnl:content", meta.get("jcr:primaryType"));
        assertTrue(meta.get("superTypes").toString().matches(
                "\\[(mix:created, )?mix:referenceable, nt:base, nt:hierarchyNode\\]"));
        assertEquals(1, meta.get("depth"));
        assertNotNull("jcr:created", meta.get("jcr:created"));
        assertEquals(0, ((List) meta.get("mixinNodeTypes")).size());
    }

    @Test
    public void testInclusionOfDocumentInDms() throws Exception {
        Node node = setUpDmsHierarchy();
        List result = toJsonAndBack(node);
        Map first = (Map) result.get(0);
        Map nodes = (Map) first.get("nodes");
        Map document = (Map) nodes.get("document");
        assertNotNull(document);
    }

    @Test
    public void testInclusionOfJcrProperties() throws Exception {
        Node node = setUpDmsHierarchy();
        List result = toJsonAndBack(node);
        Map first = (Map) result.get(0);
        Map nodes = (Map) first.get("nodes");
        Map document = (Map) nodes.get("document");
        Map documentProperties = (Map) document.get("properties");
        assertEquals("image/jpeg", documentProperties.get("jcr:mimeType"));
    }

    private List toJsonAndBack(Node node) throws IOException, RepositoryException {
        NodeToJsonSerializer serializer = new NodeToJsonSerializer(gen, node);
        gen.writeStartArray();
        gen.writeStartObject();
        serializer.buildJson();
        gen.close();

        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(out.toString(), List.class);
    }

    private Node setUpContentHierarchy() throws Exception {
        // Set up content
        Node root = getJcrSession("website").getRootNode();
        Node node = root.addNode("de", "mgnl:content");
        node.setProperty("title", "A Title");
        Node paragraph = node.addNode("para", "mgnl:contentNode");
        paragraph.setProperty("subtitle", "Subtitle");
        Node secondChild = node.addNode("company", "mgnl:content");
        secondChild.setProperty("title", "A new property");
        root.save();
        return node;
    }

    private Node setUpDmsHierarchy() throws Exception {
        // Set up content
        Node root = getJcrSession("website").getRootNode();
        Node node = root.addNode("image", "mgnl:contentNode");
        node.setProperty("title", "A title");
        Node document = node.addNode("document", "mgnl:resource");
        document.setProperty("jcr:data", "sdf", PropertyType.BINARY);
        document.setProperty("jcr:mimeType", "image/jpeg");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        document.setProperty("jcr:lastModified", calendar);
        document.setProperty("width", 400);
        root.save();
        return node;
    }

}
