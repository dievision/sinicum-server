package com.dievision.sinicum.server.jcr;


import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class CacheMarker {
    private static final Logger logger = LoggerFactory.getLogger(CacheMarker.class);
    private static List<Integer> hashCodes = new ArrayList<Integer>();

    public void addNode(Node node) {
        hashCodes.add(node.hashCode());
    }

    public String getETag() {
        int etag = 0;
        for (int i = 0; i < hashCodes.size(); i++) {
            etag = etag * (hashCodes.get(i) + i);
        }
        return etag != 0 ? "W/\"" + Integer.toString(etag) + "\"" : null;
    }
}
