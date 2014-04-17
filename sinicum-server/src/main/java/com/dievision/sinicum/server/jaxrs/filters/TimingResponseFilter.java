package com.dievision.sinicum.server.jaxrs.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;

public class TimingResponseFilter implements ContainerRequestFilter, ContainerResponseFilter {
    private static ThreadLocal<Long> timer = new ThreadLocal<Long>();
    private static final String RUNTIME_HEADER = "X-Runtime";
    private static final Logger logger = LoggerFactory.getLogger(TimingResponseFilter.class);

    @Override
    public ContainerRequest filter(ContainerRequest request) {
        timer.set(System.currentTimeMillis());
        return request;
    }

    @Override
    public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
        double responseTime = (System.currentTimeMillis() - timer.get()) / 1000.0;
        response.getHttpHeaders().add(RUNTIME_HEADER, responseTime);
        return response;
    }
}

