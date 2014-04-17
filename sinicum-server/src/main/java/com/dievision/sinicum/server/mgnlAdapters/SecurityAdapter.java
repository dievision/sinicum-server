package com.dievision.sinicum.server.mgnlAdapters;

import javax.security.auth.Subject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SecurityAdapter {
    private static SecurityAdapterInterface securityAdapter;
    private static final Logger logger = LoggerFactory.getLogger(SecurityAdapter.class);

    private SecurityAdapter() {
    }

    public static void setSecurityAdapter(SecurityAdapterInterface securityAdapterInterface) {
        securityAdapter = securityAdapterInterface;
    }

    public static Subject getSystemSubject() {
        return securityAdapter.getSystemSubject();
    }
}
