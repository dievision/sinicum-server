package com.dievision.sinicum.server.jaxrs;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class NotFoundException extends WebApplicationException {
    private static final Logger logger = LoggerFactory.getLogger(NotFoundException.class);

    public NotFoundException(String message) {
        super(Response.status(Response.Status.NOT_FOUND)
                .entity(JaxRsExceptionHelper.formatMessage(message)).
                        type(MediaType.APPLICATION_JSON).build());
    }
}
