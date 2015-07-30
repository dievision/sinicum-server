package com.dievision.sinicum.server.filters;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;


public class ProxyFilterConfigTest {
    private static final Logger logger = LoggerFactory.getLogger(ProxyFilterConfigTest.class);

    @Test
    public void testDefaultPattern() {
        assertTrue(ProxyFilterConfig.getInstance().matchesPath("/.*"));
        assertTrue(ProxyFilterConfig.getInstance().matchesPath("/something"));
    }

    @Test
    public void testSetNewPattern() {
        ProxyFilterConfig.getInstance().setProxyPattern("/something.*");
        ProxyFilterConfig config = ProxyFilterConfig.getInstance();
        assertTrue(config.matchesPath("/something"));
        assertTrue(config.matchesPath("/somethingnew"));
        assertTrue(config.matchesPath("/something/more"));

        assertFalse(config.matchesPath("/some"));
        assertFalse(config.matchesPath("/other"));
        assertFalse(config.matchesPath("/"));
    }

}
