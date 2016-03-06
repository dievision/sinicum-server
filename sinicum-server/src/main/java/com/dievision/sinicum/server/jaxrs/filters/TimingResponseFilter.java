package com.dievision.sinicum.server.jaxrs.filters;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TimingResponseFilter implements ContainerRequestFilter, ContainerResponseFilter {
    private static ThreadLocal<Long> timer = new ThreadLocal<Long>();
    private static final String RUNTIME_HEADER = "X-Runtime";
    private static final Logger logger = LoggerFactory.getLogger(TimingResponseFilter.class);

    public void filter(ContainerRequestContext requestContext) throws IOException {
        timer.set(System.currentTimeMillis());
    }

    public void filter(ContainerRequestContext requestContext,
            ContainerResponseContext responseContext) throws IOException {
        double responseTime = (System.currentTimeMillis() - timer.get()) / 1000.0;
        responseContext.getHeaders().add(RUNTIME_HEADER, responseTime);
    }
}

