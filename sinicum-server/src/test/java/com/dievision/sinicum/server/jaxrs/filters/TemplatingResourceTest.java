package com.dievision.sinicum.server.jaxrs.filters;

import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TemplatingResourceTest extends JerseyTest {
    private static final Logger logger = LoggerFactory.getLogger(TemplatingResourceTest.class);

    public TemplatingResourceTest() {
        /*
        super(new WebAppDescriptor.Builder("com.dievision.sinicum.server.resources")
                .servletClass(ServletContainer.class)
                .initParam("com.sun.jersey.spi.container.ContainerRequestFilters",
                        "com.dievision.sinicum.server.jaxrs.filters.TimingResponseFilter")
                .initParam("com.sun.jersey.spi.container.ContainerResponseFilters",
                        "com.dievision.sinicum.server.jaxrs.filters.TimingResponseFilter")
                .build());
        */
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() {
        return new GrizzlyWebTestContainerFactory();
    }

    // @Test
    public void testExample() {
        /*
        WebResource ws = resource().path(
                "templating/dialogs/component/shure/boxes/box_title_slideshow");
        ClientResponse response = ws.accept(MediaType.APPLICATION_JSON_TYPE)
                .get(ClientResponse.class);
        assertEquals(200, response.getStatus());
        */
    }
}
