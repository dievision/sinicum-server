package com.dievision.sinicum.server.mgnlAdapters;

import java.util.Collection;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.security.auth.Subject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MgnlContextAdapter {
    private static MgnlContextAdapterInterface contextAdapter;
    private static final Logger logger = LoggerFactory.getLogger(MgnlContextAdapter.class);

    private MgnlContextAdapter() {
        // nothing
    }

    public static void setContextAdapter(MgnlContextAdapterInterface mgnlContextAdapter) {
        contextAdapter = mgnlContextAdapter;
    }

    public static Collection<String> getUserRoles() {
        return contextAdapter.getUserRoles();
    }

    public static Session getJcrSession(String workspace) throws RepositoryException {
        return contextAdapter.getJcrSession(workspace);
    }

    public static void login(Subject subject) {
        contextAdapter.login(subject);
    }

}
