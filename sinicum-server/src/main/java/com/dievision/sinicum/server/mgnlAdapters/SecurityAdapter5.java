package com.dievision.sinicum.server.mgnlAdapters;

import javax.security.auth.Subject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.cms.security.Security;

public class SecurityAdapter5 implements SecurityAdapterInterface {
    private static final Logger logger = LoggerFactory.getLogger(SecurityAdapter5.class);

    @Override
    public Subject getSystemSubject() {
        return Security.getSystemSubject();
    }
}
