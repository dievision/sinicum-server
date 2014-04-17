package com.dievision.sinicum.server.resources;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dievision.sinicum.server.jcr.templating.AreaInitializer;
import com.dievision.sinicum.server.jcr.templating.DialogResolver;
import com.dievision.sinicum.server.jcr.templating.PageAreaResolver;

@Path("/_templating")
@Produces(MediaType.APPLICATION_JSON)
public class TemplatingResource {
    private static final Logger logger = LoggerFactory.getLogger(TemplatingResource.class);

    @GET
    @Path("/components/{moduleName}/{pageName}/{areaName: .+}")
    public PageAreaResolver componentsForPage(@PathParam("moduleName") String moduleName,
            @PathParam("pageName") String pageName, @PathParam("areaName") String areaName) {
        PageAreaResolver resolver = new PageAreaResolver(moduleName, pageName, areaName);
        return resolver;
    }

    @GET
    @Path("/dialogs/{componentType}/{moduleName}/{componentName: .+}")
    public DialogResolver pagePropertiesForPage(@PathParam("componentType") String componentType,
            @PathParam("moduleName") String moduleName,
            @PathParam("componentName") String componentName) {
        DialogResolver resolver = new DialogResolver(componentType, moduleName, componentName);
        return resolver;
    }

    @POST
    @Path("/areas/initialize")
    public AreaInitializer initializeArea(@FormParam("workspace") String workspace,
            @FormParam("baseNodeUuid") String baseNodeUuid,
            @FormParam("areaName") String areaName) {
        AreaInitializer areaInitializer = new AreaInitializer(workspace, baseNodeUuid, areaName);
        return areaInitializer;
    }
}
