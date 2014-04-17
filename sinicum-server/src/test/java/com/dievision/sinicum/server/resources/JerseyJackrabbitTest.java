package com.dievision.sinicum.server.resources;

import javax.jcr.Session;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.spi.container.servlet.ServletContainer;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;

import com.dievision.sinicum.server.JackrabbitTestDelegator;
import com.dievision.sinicum.server.JackrabbitTestDelegator45;

public abstract class JerseyJackrabbitTest extends JerseyTest {
    private JackrabbitTestDelegator delegator;
    private static final Logger logger = LoggerFactory.getLogger(JerseyJackrabbitTest.class);

    public JerseyJackrabbitTest() {
        super(new WebAppDescriptor.Builder("com.dievision.sinicum.server.resources")
                .servletClass(ServletContainer.class)
                .initParam("com.sun.jersey.spi.container.ContainerRequestFilters",
                        "com.dievision.sinicum.server.jaxrs.filters.PrettyPrintFilter")
                .build());
        delegator = new JackrabbitTestDelegator45();
    }

    @BeforeClass
    public static void beforeAll() throws Exception {
        JackrabbitTestDelegator.setUpRepository();
    }

    @Before
    public void setUpContextAdapter() {
        delegator.setUpBeforeTest();
    }

    @After
    public void shutdownRepository() throws Exception {
        delegator.shutDownAfterTest();
    }

    @AfterClass
    public static void afterAll() throws Exception {
        JackrabbitTestDelegator.shutDownRepository();
    }

    protected Session getJcrSession(String workspaceName) {
        return delegator.getJcrSession(workspaceName);
    }
}
