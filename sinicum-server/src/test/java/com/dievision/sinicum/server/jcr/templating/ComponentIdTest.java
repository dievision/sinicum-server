package com.dievision.sinicum.server.jcr.templating;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

public class ComponentIdTest {

    private static final String PAGE_STRING = "myModule:pages/homepage";
    private static final String COMPONENT_STRING = "myModule:components/teaser";
    private static final Logger logger =
            LoggerFactory.getLogger(ComponentIdTest.class);

    @Test
    public void testPageComponentResolution() {
        ComponentId translator = new ComponentId(PAGE_STRING);
        assertEquals("myModule", translator.getComponent());
    }

    @Test
    public void testPageType() {
        ComponentId translator = new ComponentId(PAGE_STRING);
        assertEquals(ComponentId.PAGE_TYPE, translator.getType());
    }

    @Test
    public void testPagePath() {
        ComponentId translator = new ComponentId(PAGE_STRING);
        assertEquals("homepage", translator.getPath());
    }

    @Test
    public void testPageParentPathElement() {
        ComponentId translator = new ComponentId(PAGE_STRING);
        assertEquals("pages", translator.parentPathElement());
    }

    @Test
    public void testComponentComponentResolution() {
        ComponentId translator = new ComponentId(COMPONENT_STRING);
        assertEquals("myModule", translator.getComponent());
    }

    @Test
    public void testComponentType() {
        ComponentId translator = new ComponentId(COMPONENT_STRING);
        assertEquals(ComponentId.COMPONENT_TYPE, translator.getType());
    }

    @Test
    public void testComponentPath() {
        ComponentId translator = new ComponentId(COMPONENT_STRING);
        assertEquals("teaser", translator.getPath());
    }

    @Test
    public void testComponentParentPathElement() {
        ComponentId translator = new ComponentId(COMPONENT_STRING);
        assertEquals("components", translator.parentPathElement());
    }

}
