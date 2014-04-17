package com.dievision.sinicum.server.magnolia;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.rendering.context.RenderingContext;
import info.magnolia.rendering.engine.RenderException;
import info.magnolia.rendering.renderer.Renderer;

/**
 *
 */
public class SinicumTemplateRenderer implements Renderer {
    private static final Logger logger = LoggerFactory.getLogger(SinicumTemplateRenderer.class);

    @Override
    public void render(RenderingContext renderingContext, Map<String, Object> stringObjectMap)
        throws RenderException {
        // nothing
    }
}
