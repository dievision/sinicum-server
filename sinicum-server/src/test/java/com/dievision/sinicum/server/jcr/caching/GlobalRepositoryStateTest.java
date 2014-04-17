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
        String key = GlobalRepositoryState.getCacheKey();
        assertTrue(key.matches(".{40}"));
    }

    @Test
    public void testUpdateCacheKey() {
        String key = GlobalRepositoryState.getCacheKey();
        GlobalRepositoryState.updateCacheKey();
        assertFalse(key.equals(GlobalRepositoryState.getCacheKey()));
    }
}
