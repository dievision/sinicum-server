package com.dievision.sinicum.server.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dievision.sinicum.server.jcr.caching.GlobalRepositoryState;

@Path("_cache")
@Produces(MediaType.APPLICATION_JSON)
public class CacheResource {
    private static final Logger logger = LoggerFactory.getLogger(CacheResource.class);

    @GET
    @Path("/global")
    public GlobalCacheKeyWrapper globalKey() {
        return new GlobalCacheKeyWrapper();
    }

    private static class GlobalCacheKeyWrapper {
        public String getCacheKey() {
            return GlobalRepositoryState.getCacheKey();
        }
    }
}
