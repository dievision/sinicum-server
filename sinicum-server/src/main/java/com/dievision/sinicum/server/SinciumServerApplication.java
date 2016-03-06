package com.dievision.sinicum.server;

import org.glassfish.jersey.message.GZipEncoder;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dievision.sinicum.server.jaxrs.filters.LoginFilter;
import com.dievision.sinicum.server.jaxrs.filters.PrettyPrintFilter;
import com.dievision.sinicum.server.jaxrs.filters.TimingResponseFilter;

public class SinciumServerApplication extends ResourceConfig {
    private static final Logger logger = LoggerFactory.getLogger(SinciumServerApplication.class);

    public SinciumServerApplication() {
        super(
                TimingResponseFilter.class,
                LoginFilter.class,
                PrettyPrintFilter.class,
                GZipEncoder.class
        );
        packages("com.dievision.sinicum.server.resources");
    }
}
