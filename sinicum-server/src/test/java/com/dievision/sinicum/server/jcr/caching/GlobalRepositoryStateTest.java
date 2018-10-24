package com.dievision.sinicum.server.jcr.caching;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GlobalRepositoryStateTest {
    private static final Logger logger = LoggerFactory.getLogger(GlobalRepositoryStateTest.class);

    @Test
    public void testReturnCacheKey() {
        String key = GlobalRepositoryState.getCacheKey("dievision");
        assertTrue(key.matches(".{40}"));
    }

    @Test
    public void testDifferentCacheKeys() {
        String keyDievision = GlobalRepositoryState.getCacheKey("dievision");
        String keyCody = GlobalRepositoryState.getCacheKey("cody");
        assertFalse(keyDievision.equals(keyCody));
    }
}
