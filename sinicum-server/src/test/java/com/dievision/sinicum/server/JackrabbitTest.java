package com.dievision.sinicum.server;

import java.io.IOException;
import java.io.InputStream;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Base class for tests that have to use a working repository. Call <code>startRepository</code>
 * from your test method to actually start the repository.
 */
public abstract class JackrabbitTest {
    private static final String VERSION_2_IDENTIFIER = "2.0";
    private JackrabbitTestDelegator delegator;

    private static final Logger logger = LoggerFactory.getLogger(JackrabbitTest.class);

    public JackrabbitTest() {
        delegator = new JackrabbitTestDelegator();
    }

    public JackrabbitTest(String nodeTypeDefinition) {
        delegator = new JackrabbitTestDelegator(nodeTypeDefinition);
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

    protected void importFromExportXml(Node parentNode, String resourcePath)
        throws IOException, RepositoryException {
        if (parentNode.getSession().hasPendingChanges()) {
            parentNode.getSession().save();
        }
        InputStream stream = getClass().getResourceAsStream(resourcePath);
        getJcrSession("config").getWorkspace().importXML(parentNode.getPath(), stream,
                ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING);
        stream.close();
        parentNode.getSession().save();
    }

    protected boolean isVersion2() {
        String version = getJcrSession("website").getRepository().
                getDescriptor(Repository.SPEC_VERSION_DESC);
        return VERSION_2_IDENTIFIER.equals(version);
    }
}
