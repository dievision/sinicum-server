package com.dievision.sinicum.server.jaxrs;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class NotAllowedException extends WebApplicationException {
    private static final Logger logger = LoggerFactory.getLogger(NotAllowedException.class);

    public NotAllowedException(String message) {
        super(Response.status(Response.Status.FORBIDDEN)
                .entity(JaxRsExceptionHelper.formatMessage(message)).
                        type(MediaType.APPLICATION_JSON).build());
    }
}
