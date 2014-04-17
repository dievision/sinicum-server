package com.dievision.sinicum.server.jcr.templating;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComponentId {
    private final String componentString;
    private String component;
    private int type = -1;
    private String path;

    public static final int PAGE_TYPE = 0;
    public static final int COMPONENT_TYPE = 1;
    private static final String PAGES_PREFIX = "pages/";
    private static final String PAGES_PARENT_PATH_ELEMENT = "pages";
    private static final String COMPONENTS_PREFIX = "components/";
    private static final String COMPONENTS_PARENT_PATH_ELEMENT = "components";
    private static final Logger logger = LoggerFactory.getLogger(ComponentId.class);

    public ComponentId(String componentString) {
        this.componentString = componentString;
        splitComponents();
    }

    public String getComponent() {
        return component;
    }

    public int getType() {
        return type;
    }

    public String getPath() {
        return path;
    }

    public String parentPathElement() {
        if (PAGE_TYPE == getType()) {
            return PAGES_PARENT_PATH_ELEMENT;
        } else if (COMPONENT_TYPE == getType()) {
            return COMPONENTS_PARENT_PATH_ELEMENT;
        }
        return null;
    }

    private void splitComponents() {
        if (componentString.indexOf(":") > 0) {
            String[] parts = componentString.split(":");
            this.component = parts[0];
            if (parts[1].startsWith(PAGES_PREFIX)) {
                this.type = PAGE_TYPE;
                this.path = parts[1].substring(PAGES_PREFIX.length(), parts[1].length());
            } else if (parts[1].startsWith(COMPONENTS_PREFIX)) {
                this.type = COMPONENT_TYPE;
                this.path = parts[1].substring(COMPONENTS_PREFIX.length(), parts[1].length());
            }
        }
    }
}
