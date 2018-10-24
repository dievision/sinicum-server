package com.dievision.sinicum.server.mgnlAdapters;

import java.util.Collection;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.security.auth.Subject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.context.MgnlContext;

public class MgnlContextAdapter5 implements MgnlContextAdapterInterface {
    private static final Logger logger = LoggerFactory.getLogger(MgnlContextAdapter5.class);

    @Override
    public Collection<String> getUserRoles() {
        return MgnlContext.getUser().getRoles();
    }

    @Override
    public Session getJcrSession(String workspace) throws RepositoryException {
        return MgnlContext.getJCRSession(workspace);
    }

    @Override
    public void login(Subject subject) {
        MgnlContext.login(subject);
    }
}
