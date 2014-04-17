package com.dievision.sinicum.server.resources;

import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import com.dievision.sinicum.server.jcr.caching.GlobalRepositoryState;

import static org.junit.Assert.assertEquals;

public class CacheResourceTest extends JerseyJackrabbitTest {
    private static final Logger logger = LoggerFactory.getLogger(CacheResourceTest.class);

    @Test
    public void testGlobalKey() {
        WebResource ws = resource().path("/_cache/global")
                .queryParam("pretty", "true");
        ClientResponse response = ws.accept(MediaType.APPLICATION_JSON_TYPE)
                .get(ClientResponse.class);
        assertEquals(200, response.getStatus());
        Map map = response.getEntity(Map.class);
        assertEquals(GlobalRepositoryState.getCacheKey(), map.get("cacheKey"));
    }
}
