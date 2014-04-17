package com.dievision.sinicum.server.jcr.caching;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GlobalRepositoryState {
    private static String cacheKey;
    private static final Logger logger = LoggerFactory.getLogger(GlobalRepositoryState.class);

    private GlobalRepositoryState() {
        // nothing
    }

    public static String getCacheKey() {
        if (cacheKey == null) {
            updateCacheKey();
        }
        return cacheKey;
    }

    public static void updateCacheKey() {
        String time = Long.toString(System.currentTimeMillis());
        cacheKey = DigestUtils.shaHex(time);
    }
}
