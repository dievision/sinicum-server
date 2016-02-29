package com.dievision.sinicum.server.jaxrs.filters;

import org.glassfish.jersey.test.JerseyTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginFilterTest extends JerseyTest {
    private static final Logger logger = LoggerFactory.getLogger(LoginFilterTest.class);

    public LoginFilterTest() {
        /*
        super(new WebAppDescriptor.Builder("com.dievision.sinicum.server.resources")
                .servletClass(ServletContainer.class)
                .initParam("com.sun.jersey.spi.container.ContainerRequestFilters",
                        "com.dievision.sinicum.server.jaxrs.filters.LoginFilter")
                .build());
        */
    }

    public void testExample() {
        /*
        WebResource ws = resource().path("test/hello");
        ws.addFilter(new HTTPBasicAuthFilter("user", "pass"));
        ClientResponse response = ws.accept("text/plain")
                .get(ClientResponse.class);
        assertEquals(200, response.getStatus());
        */
    }

}
