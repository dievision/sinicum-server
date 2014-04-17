package com.dievision.sinicum.server.mgnlAdapters;

import java.util.Arrays;
import java.util.Collection;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.security.auth.Subject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dievision.sinicum.server.JackrabbitTestDelegator;

public class MgnlContextAdapterTest implements MgnlContextAdapterInterface {
    private final JackrabbitTestDelegator delegator;
    private static final Logger logger = LoggerFactory.getLogger(MgnlContextAdapterTest.class);

    public MgnlContextAdapterTest(JackrabbitTestDelegator delegator) {
        this.delegator = delegator;
    }

    @Override
    public Collection<String> getUserRoles() {
        return Arrays.asList("sinicum-server");
    }

    @Override
    public Session getJcrSession(String workspace) throws RepositoryException {
        return delegator.getJcrSession(workspace);
    }

    @Override
    public void login(Subject subject) {
        // nothing
    }
}
