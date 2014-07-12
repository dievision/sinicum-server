package com.dievision.sinicum.server.jcr;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.jcr.Node;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static junit.framework.Assert.*;

import com.dievision.sinicum.server.JackrabbitTest45;

/**
 *
 */
public class NodeApiWrapper4Test extends JackrabbitTest45 {
    private static final Logger logger = LoggerFactory.getLogger(NodeApiWrapper4Test.class);

    private Calendar calendar;

    @Test
    public void testJsonContainsProperties() throws Exception {
        NodeApiWrapper wrapper = getWrapper(setUpContentHierarchy());
        Map<String, Object> properties = wrapper.getProperties();
        assertNotNull(properties.get("title"));
    }

    @Test
    public void testJsonContainsContentNodes() throws Exception {
        NodeApiWrapper wrapper = getWrapper(setUpContentHierarchy());
        Map<String, Object> nodes = wrapper.getNodes();
        assertNotNull(nodes.get("para"));
    }

    @Test
    public void testJsonContainsNoNodesOfSameType() throws Exception {
        NodeApiWrapper wrapper = getWrapper(setUpContentHierarchy());
        Map<String, Object> nodes = wrapper.getNodes();
        assertNull(nodes.get("company"));
    }

    @Test
    public void testJsonContainsMetadata() throws Exception {
        NodeApiWrapper wrapper = getWrapper(setUpContentHierarchy());
        NodeApiWrapperMeta meta = wrapper.getMeta();
        assertNotNull(meta);
    }

    @Test
    public void testInclusionOfDocumentInDms() throws Exception {
        NodeApiWrapper wrapper = getWrapper(setUpDmsHierarchy());
        NodeApiWrapper document = (NodeApiWrapper) wrapper.getNodes().get("document");
        assertNotNull(document);
    }

    @Test
    public void testInclusionOfJcrProperties() throws Exception {
        NodeApiWrapper wrapper = getWrapper(setUpDmsHierarchy());
        NodeApiWrapper document = (NodeApiWrapper) wrapper.getNodes().get("document");
        Map<String, Object> properties = document.getProperties();
        assertEquals("image/jpeg", properties.get("jcr:mimeType"));
    }

    @Test
    public void testDateHandling() throws Exception {
        NodeApiWrapper wrapper = getWrapper(setUpContentHierarchy());
        String date = (String) wrapper.getProperties().get("press_date");
        assertEquals("2014-07-03T22:00:00.000Z", date);
    }

    @Test
    public void testMultiValueNodes() throws Exception {
        NodeApiWrapper wrapper = getWrapper(addMultiValueToHierarchy(setUpContentHierarchy()));
        List<Object> result = (List<Object>) wrapper.getProperties().get("mutivalue");
        assertEquals("value1", result.get(0));
        assertEquals("value2", result.get(1));
        assertEquals(2, result.size());
    }

    private NodeApiWrapper getWrapper(Node node) throws IOException, RepositoryException {
        NodeApiWrapper nodeApiWrapper = new NodeApiWrapper4(node, node.getPrimaryNodeType());
        return nodeApiWrapper;
    }

    private Node setUpContentHierarchy() throws Exception {
        // Set up content
        Session session = getJcrSession("website");
        Node root = session.getRootNode();
        Node node = root.addNode("de", "mgnl:page");
        node.setProperty("title", "A Title");
        node.setProperty("press_date", getCalendar());
        Node component = node.addNode("para", "mgnl:component");
        component.setProperty("subtitle", "Subtitle");
        Node childPage = node.addNode("company", "mgnl:page");
        childPage.setProperty("title", "A new property");
        session.save();
        return node;
    }

    private Node addMultiValueToHierarchy(Node node) throws RepositoryException {
        node.setProperty("mutivalue", new String[]{"value1", "value2"});
        return node;
    }

    private Node setUpDmsHierarchy() throws Exception {
        // Set up content
        Session session = getJcrSession("dms");
        Node root = session.getRootNode();
        Node node = root.addNode("image", "mgnl:contentNode");
        node.setProperty("title", "A title");
        Node document = node.addNode("document", "mgnl:resource");
        document.setProperty("jcr:data", "sdf", PropertyType.BINARY);
        document.setProperty("jcr:mimeType", "image/jpeg");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        document.setProperty("jcr:lastModified", calendar);
        document.setProperty("width", 400);
        session.save();
        return node;
    }

    private Calendar getCalendar() {
        if (calendar == null) {
            calendar = Calendar.getInstance();
            calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
            calendar.set(Calendar.YEAR, 2014);
            calendar.set(Calendar.MONTH, 6);
            calendar.set(Calendar.DAY_OF_MONTH, 3);
            calendar.set(Calendar.HOUR_OF_DAY, 22);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
        }
        return calendar;
    }
}
