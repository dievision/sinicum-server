package com.dievision.sinicum.server.resources;

import java.util.Arrays;
import java.util.List;

import javax.jcr.ItemNotFoundException;
import javax.jcr.PathNotFoundException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dievision.sinicum.server.jaxrs.GeneralErrorException;
import com.dievision.sinicum.server.jaxrs.NotFoundException;
import com.dievision.sinicum.server.jcr.NavigationElement;
import com.dievision.sinicum.server.jcr.NavigationProvider;
import com.dievision.sinicum.server.jcr.ParentNavigationProvider;

@Path("/_navigation")
@Produces(MediaType.APPLICATION_JSON)
public class NavigationResource {
    private static final String PROPERTIES_SEPARATOR = ";";
    private static final Logger logger = LoggerFactory.getLogger(NavigationResource.class);

    @GET
    @Path("/children/{baseNode: .+}")
    public List<NavigationElement> navigationForNode(@PathParam("baseNode") String baseNode,
            @QueryParam("properties") String properties, @QueryParam("depth") int depth) {
        try {

            NavigationProvider provider = new NavigationProvider(baseNode,
                    resolveProperties(properties), depth);
            return provider.getNavigationElements();
        } catch (ItemNotFoundException e) {
            throw new NotFoundException("Base node could not be found.");
        } catch (PathNotFoundException e) {
            throw new NotFoundException("Base node could not be found.");
        } catch (Exception e) {
            throw new GeneralErrorException(e);
        }
    }

    @GET
    @Path("/parents/{baseNode: .+}")
    public List<NavigationElement> parentsForNode(@PathParam("baseNode") String baseNode,
            @QueryParam("properties") String properties) {
        try {

            ParentNavigationProvider provider = new ParentNavigationProvider(baseNode,
                    resolveProperties(properties));
            return provider.getNavigationElements();
        } catch (ItemNotFoundException e) {
            throw new NotFoundException("Base node could not be found.");
        } catch (PathNotFoundException e) {
            throw new NotFoundException("Base node could not be found.");
        } catch (Exception e) {
            throw new GeneralErrorException(e);
        }
    }

    private List<String> resolveProperties(String propertiesParam) {
        List<String> result;
        if (propertiesParam.contains(PROPERTIES_SEPARATOR)) {
            result = Arrays.asList(propertiesParam.split(PROPERTIES_SEPARATOR));
        } else {
            result = Arrays.asList(propertiesParam);
        }
        return result;
    }

}
