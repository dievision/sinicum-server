package com.dievision.sinicum.server.jaxrs.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
import com.sun.jersey.test.framework.spi.container.TestContainerFactory;
import com.sun.jersey.test.framework.spi.container.grizzly2.web.GrizzlyWebTestContainerFactory;

import static org.junit.Assert.assertEquals;

public class LoginFilterTest extends JerseyTest {
    private static final Logger logger = LoggerFactory.getLogger(LoginFilterTest.class);

    public LoginFilterTest() {
        super(new WebAppDescriptor.Builder("com.dievision.sinicum.server.resources")
                .servletClass(ServletContainer.class)
                .initParam("com.sun.jersey.spi.container.ContainerRequestFilters",
                        "com.dievision.sinicum.server.jaxrs.filters.LoginFilter")
                .build());
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() {
        return new GrizzlyWebTestContainerFactory();
    }

    public void testExample() {
        WebResource ws = resource().path("test/hello");
        ws.addFilter(new HTTPBasicAuthFilter("user", "pass"));
        ClientResponse response = ws.accept("text/plain")
                .get(ClientResponse.class);
        assertEquals(200, response.getStatus());
    }

}
