package com.dievision.sinicum.server.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Produces(MediaType.TEXT_PLAIN)
@Path("/test")
public class TestResource {
    private static final Logger logger = LoggerFactory.getLogger(TestResource.class);

    @GET
    @Path("/hello")
    public String hello() {
        return "world";
    }

}
