package com.dievision.sinicum.server.resources;

import javax.jcr.Session;
import javax.ws.rs.core.Application;

import org.glassfish.jersey.test.JerseyTest;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



import com.dievision.sinicum.server.JackrabbitTestDelegator;
import com.dievision.sinicum.server.JackrabbitTestDelegator45;

public abstract class JerseyJackrabbitTest extends JerseyTest {
    private JackrabbitTestDelegator delegator;
    private static final Logger logger = LoggerFactory.getLogger(JerseyJackrabbitTest.class);

    public JerseyJackrabbitTest() {
        delegator = new JackrabbitTestDelegator45();
    }

    @Override
    protected Application configure() {
        return super.configure();
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
