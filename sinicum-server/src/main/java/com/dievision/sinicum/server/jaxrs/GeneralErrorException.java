package com.dievision.sinicum.server.jaxrs;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class GeneralErrorException extends WebApplicationException {
    private static final Logger logger = LoggerFactory.getLogger(GeneralErrorException.class);

    public GeneralErrorException(Throwable cause) {
        super(Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(JaxRsExceptionHelper.formatMessage(cause.getMessage())).
                                type(MediaType.APPLICATION_JSON).build());
    }

}
