package com.dievision.sinicum.server;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.apache.jackrabbit.api.JackrabbitNodeTypeManager;
import org.apache.jackrabbit.api.JackrabbitRepository;
import org.apache.jackrabbit.api.JackrabbitWorkspace;
import org.apache.jackrabbit.core.TransientRepository;
import org.apache.jackrabbit.core.fs.local.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dievision.sinicum.server.mgnlAdapters.MgnlContextAdapter;
import com.dievision.sinicum.server.mgnlAdapters.MgnlContextAdapterTest;

public class JackrabbitTestDelegator {
    private String nodeTypeDefinition;
    private Session defaultSession;
    private Map<String, Session> sessionCache = new HashMap<String, Session>();

    private static final File JACKRABBIT_DIRECTORY_PATH = new File("target/testdata/repository");
    private static final String JACKRABBIT_CONFIG_PATH = "test-repository.xml";
    private static final String MAGNOLIA_NODE_TYPE_DEF = "mgnl-nodetypes/magnolia-nodetypes.xml";
    private static Repository repository;
    private static final Logger logger = LoggerFactory.getLogger(JackrabbitTestDelegator.class);

    public JackrabbitTestDelegator() {
        this(MAGNOLIA_NODE_TYPE_DEF);
    }

    public JackrabbitTestDelegator(String nodeTypeDefinition) {
        setNodeTypeDefinition(nodeTypeDefinition);
    }

    public static void setUpRepository() throws IOException {
        if (JACKRABBIT_DIRECTORY_PATH.exists()) {
            FileUtil.delete(JACKRABBIT_DIRECTORY_PATH);
        }
        repository = new TransientRepository(new File(getPathFromResource(JACKRABBIT_CONFIG_PATH)),
                JACKRABBIT_DIRECTORY_PATH);

    }

    public static void shutDownRepository() throws IOException {
        try {
            JackrabbitRepository jackrabbitRepository = (JackrabbitRepository) repository;
            jackrabbitRepository.shutdown();
        } finally {
            if (JACKRABBIT_DIRECTORY_PATH.exists()) {
                FileUtil.delete(JACKRABBIT_DIRECTORY_PATH);
            }
        }
    }

    public void setUpBeforeTest() {
        MgnlContextAdapter.setContextAdapter(new MgnlContextAdapterTest(this));
    }

    private static String getPathFromResource(String resourcePath) {
        URL resource = Thread.currentThread().getContextClassLoader().getResource(resourcePath);
        if (resource != null) {
            return resource.getFile();
        } else {
            return null;
        }
    }

    public void shutDownAfterTest() {
        for (String workspaceName : sessionCache.keySet()) {
            Session session = sessionCache.get(workspaceName);
            if (session != null) {
                try {
                    session.logout();
                } catch (Exception e) {
                    // nothing
                }
            }
        }
        sessionCache = null;

        if (defaultSession != null) {
            try {
                defaultSession.logout();
            } finally {
                defaultSession = null;
            }
        }
    }

    public Session getJcrSession(String workspaceName) {
        if (!sessionCache.containsKey(workspaceName)) {
            try {
                Session jcrSession;
                if (defaultSession == null) {
                    defaultSession = repository.login(
                            new SimpleCredentials("anonymousId", "anonymous".toCharArray()));
                }
                try {
                    jcrSession = repository.login(
                            new SimpleCredentials("anonymousId", "anonymous".toCharArray()),
                            workspaceName);
                } catch (NoSuchWorkspaceException e) {
                    jcrSession = createWorkspace(workspaceName, defaultSession);
                }
                if (!mgnlNamespacesRegistered(jcrSession)) {
                    registerNamespaces(jcrSession);
                }
                sessionCache.put(workspaceName, jcrSession);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return sessionCache.get(workspaceName);

    }

    private void registerNamespaces(Session jcrSession) {
        try {
            JackrabbitNodeTypeManager nodeTypeManager = (JackrabbitNodeTypeManager)
                jcrSession.getWorkspace().getNodeTypeManager();
            InputStream is = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(getMagnoliaNodeTypeDef());
            nodeTypeManager.registerNodeTypes(is, JackrabbitNodeTypeManager.TEXT_XML);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean mgnlNamespacesRegistered(Session jcrSession) throws RepositoryException {
        boolean registered = false;
        for (String prefix : jcrSession.getNamespacePrefixes()) {
            if ("mgnl".equals(prefix)) {
                registered = true;
                break;
            }
        }
        return registered;
    }

    private Session createWorkspace(String workspaceName, Session jcrSession) {
        Session session;
        JackrabbitWorkspace workspace = (JackrabbitWorkspace) jcrSession.getWorkspace();
        try {
            workspace.createWorkspace(workspaceName);
            session = repository.login(
                    new SimpleCredentials("anonymousId", "anonymous".toCharArray()),
                    workspaceName);
        } catch (RepositoryException e) {
            throw new RuntimeException(e);
        }
        return session;
    }

    protected void setNodeTypeDefinition(String nodeTypeDefinition) {
        this.nodeTypeDefinition = nodeTypeDefinition;
    }

    protected String getMagnoliaNodeTypeDef() {
        return this.nodeTypeDefinition;
    }
}
