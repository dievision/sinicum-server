package com.dievision.sinicum.server.filters;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;


public class ProxyFilterConfigTest {
    private static final Logger logger = LoggerFactory.getLogger(ProxyFilterConfigTest.class);

    @Test
    public void testSetNewPattern() throws URISyntaxException {
        ProxyEntry entry = new ProxyEntry("/something.*", "http://www.example.com");

        ProxyFilterConfig.getInstance().addProxyEntry(entry);
        ProxyFilterConfig config = ProxyFilterConfig.getInstance();
        assertTrue(config.matchesPath("/something"));
        assertTrue(config.matchesPath("/somethingnew"));
        assertTrue(config.matchesPath("/something/more"));

        assertFalse(config.matchesPath("/some"));
        assertFalse(config.matchesPath("/other"));
        assertFalse(config.matchesPath("/"));
    }

    @Test
    public void testMultipleEntries() throws URISyntaxException {
        ProxyEntry entry = new ProxyEntry("/something.*", "http://www.example.com");
        ProxyFilterConfig.getInstance().addProxyEntry(entry);
        ProxyEntry entry2 = new ProxyEntry("/otherpath.*", "http://www.dievision.de");
        ProxyFilterConfig.getInstance().addProxyEntry(entry2);
        ProxyEntry entry3 = new ProxyEntry("/.*", "http://www.the-rest.de");
        ProxyFilterConfig.getInstance().addProxyEntry(entry3);

        ProxyFilterConfig config = ProxyFilterConfig.getInstance();
        assertTrue(config.matchesPath("/something"));
        assertTrue(config.matchesPath("/otherpath"));

        assertEquals(config.getProxyTargetUri("/something/more"),
                new URI("http://www.example.com"));
        assertEquals(config.getProxyTargetUri("/otherpath"), new URI("http://www.dievision.de"));
        assertEquals(config.getProxyTargetUri("/123123123"), new URI("http://www.the-rest.de"));
    }

}
