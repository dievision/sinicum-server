package com.dievision.sinicum.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JackrabbitTest45 extends JackrabbitTest {
    public static final String NODE_TYPE_DEF = "mgnl-nodetypes/magnolia-nodetypes-45.xml";
    private static final Logger logger = LoggerFactory.getLogger(JackrabbitTest45.class);

    public JackrabbitTest45() {
        super(NODE_TYPE_DEF);
    }
}

