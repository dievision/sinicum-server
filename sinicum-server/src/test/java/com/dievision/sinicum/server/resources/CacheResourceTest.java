package com.dievision.sinicum.server.resources;

import java.util.Map;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dievision.sinicum.server.jcr.caching.GlobalRepositoryState;

import static org.junit.Assert.assertEquals;

public class CacheResourceTest extends JerseyJackrabbitTest {
    private static final Logger logger = LoggerFactory.getLogger(CacheResourceTest.class);

    @Override
    protected Application configure() {
        return new ResourceConfig(CacheResource.class);
    }

    @Test
    public void testGlobalKey() {
        Map response = target("/_cache/global").request().accept(MediaType.APPLICATION_JSON).
                get(Map.class);
        assertEquals(GlobalRepositoryState.getCacheKey(), response.get("cacheKey"));
    }
}
