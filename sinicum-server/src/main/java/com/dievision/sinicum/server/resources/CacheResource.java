package com.dievision.sinicum.server.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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

    @GET
    @Path("/site/{sitePrefix: .+}")
    public GlobalCacheKeyWrapper globalKey(@PathParam("sitePrefix") String sitePrefix) {
        return new GlobalCacheKeyWrapper(sitePrefix);
    }

    private static class GlobalCacheKeyWrapper {
        private String sitePrefix;

        public GlobalCacheKeyWrapper() {
        }

        public GlobalCacheKeyWrapper(String sitePrefix) {
            this.sitePrefix = sitePrefix;
        }

        public String getCacheKey() {
            if (sitePrefix == null) {
                return GlobalRepositoryState.getCacheKey();
            } else {
                return GlobalRepositoryState.getCacheKey(sitePrefix);
            }
        }
    }
}
