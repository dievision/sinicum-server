package com.dievision.sinicum.server.mgnlAdapters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NodeTypeAdapter {
    private static final Logger logger = LoggerFactory.getLogger(NodeTypeAdapter.class);

    private NodeTypeAdapter() {
        // nothing
    }

    public static String getContentType() {
        return "mgnl:content";
    }

    public static String getContentNodeType() {
        return "mgnl:contentNode";
    }

}
