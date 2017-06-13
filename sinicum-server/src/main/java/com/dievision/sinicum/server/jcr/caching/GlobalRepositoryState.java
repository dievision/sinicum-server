package com.dievision.sinicum.server.jcr.caching;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public final class GlobalRepositoryState {
    private static String cacheKey;
    private static HashMap<String, String> cacheKeys = new HashMap<String, String>();
    private static final Logger logger = LoggerFactory.getLogger(GlobalRepositoryState.class);

    private GlobalRepositoryState() {
        // nothing
    }

    public static String getCacheKey() {
        if (cacheKey == null) {
            GlobalRepositoryState.updateCacheKey();
        }
        return cacheKey;
    }

    public static String getCacheKey(String sitePrefix) {
        if (!cacheKeys.containsKey(sitePrefix)) {
            GlobalRepositoryState.updateCacheKey(sitePrefix);
        }
        return cacheKeys.get(sitePrefix);
    }

    public static void updateCacheKey() {
        String time = Long.toString(System.currentTimeMillis());
        cacheKey = DigestUtils.shaHex(time);
    }

    public static void updateCacheKey(String sitePrefix) {
        String time = Long.toString(System.currentTimeMillis());
        cacheKeys.put(sitePrefix, DigestUtils.shaHex(sitePrefix + time));
    }
}
