package com.dievision.sinicum.server.jaxrs.filters;

import javax.ws.rs.core.MultivaluedMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;

public class PrettyPrintFilter implements ContainerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(PrettyPrintFilter.class);

    @Override
    public ContainerRequest filter(ContainerRequest request) {
        MultivaluedMap<String, String> map = request.getQueryParameters();
        String pretty = map.getFirst("pretty");
        if ("true".equals(pretty)) {
            PrettyPrintContext.getInstance().setPrettyPrint(true);
        } else {
            PrettyPrintContext.getInstance().setPrettyPrint(false);
        }
        return request;
    }
}
