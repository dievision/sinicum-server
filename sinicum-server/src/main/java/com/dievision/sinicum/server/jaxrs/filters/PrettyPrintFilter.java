package com.dievision.sinicum.server.jaxrs.filters;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MultivaluedMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrettyPrintFilter implements ContainerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(PrettyPrintFilter.class);

    public void filter(ContainerRequestContext requestContext) throws IOException {
        MultivaluedMap<String, String> map = requestContext.getUriInfo().getQueryParameters();
        String pretty = map.getFirst("pretty");
        if ("true".equals(pretty)) {
            PrettyPrintContext.getInstance().setPrettyPrint(true);
        } else {
            PrettyPrintContext.getInstance().setPrettyPrint(false);
        }
    }
}
