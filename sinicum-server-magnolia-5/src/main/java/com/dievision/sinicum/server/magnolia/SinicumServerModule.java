package com.dievision.sinicum.server.magnolia;

import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.context.MgnlContext;
import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;

import com.dievision.sinicum.server.mgnlAdapters.MgnlContextAdapter;
import com.dievision.sinicum.server.mgnlAdapters.MgnlContextAdapter5;
import com.dievision.sinicum.server.mgnlAdapters.SecurityAdapter;
import com.dievision.sinicum.server.mgnlAdapters.SecurityAdapter5;

public class SinicumServerModule extends SinicumServerModuleBase implements ModuleLifecycle {
    private static final Logger logger = LoggerFactory.getLogger(SinicumServerModuleBase.class);

    @Override
    public void start(ModuleLifecycleContext moduleLifecycleContext) {
        setUpDependencies();
        try {
            super.start(moduleLifecycleContext.getCurrentModuleDefinition().getName(),
                    MgnlContext.getJCRSession("config"));
        } catch (RepositoryException e) {
            logger.error("Failed to start Sinicum Server Module: " + e.toString());
        }

    }

    @Override
    public void stop(ModuleLifecycleContext moduleLifecycleContext) {
    }

    private void setUpDependencies() {
        MgnlContextAdapter.setContextAdapter(new MgnlContextAdapter5());
        SecurityAdapter.setSecurityAdapter(new SecurityAdapter5());
    }

}
