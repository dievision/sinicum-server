package com.dievision.sinicum.server.mgnlAdapters;

import java.util.Collection;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.security.auth.Subject;

public interface MgnlContextAdapterInterface {
    Collection<String> getUserRoles();
    Session getJcrSession(String workspace) throws RepositoryException;
    void login(Subject subject);
}
