package com.dievision.sinicum.server.resources;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.AccessDeniedException;
import javax.jcr.Item;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dievision.sinicum.server.jaxrs.GeneralErrorException;
import com.dievision.sinicum.server.jaxrs.NotAllowedException;
import com.dievision.sinicum.server.jaxrs.NotFoundException;
import com.dievision.sinicum.server.jcr.NodeApiWrapper;
import com.dievision.sinicum.server.jcr.NodeQueryManager;
import com.dievision.sinicum.server.jcr.NodeResolver;
import com.dievision.sinicum.server.mgnlAdapters.MgnlContextAdapter;

/**
 *
 */
@Path("/{workspace}")
@Produces(MediaType.APPLICATION_JSON)
public class NodeResource {
    private static final String[] FORBIDDEN_WORKSPACES = {"users", "userroles", "usergroups"};
    private static final String SINICUM_SERVER_ROLE = "sinicum-server";
    private static final Logger logger = LoggerFactory.getLogger(NodeResource.class);

    @GET
    @Path("/{path: .+}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<NodeApiWrapper> nodeByPath(@PathParam("workspace") String workspace,
            @PathParam("path") String path,
            @QueryParam("includeChildNodeTypes") String childNodeTypes) {
        String nodePath = "/" + path;
        try {
            checkAccess(workspace);
            NodeResolver nodeResolver = new NodeResolver(getSession(workspace));
            nodeResolver.setPath(nodePath);
            List<NodeApiWrapper> result = new ArrayList<NodeApiWrapper>();
            result.add(nodeResolver.getNode());
            return result;
        } catch (ItemNotFoundException e) {
            throw new NotFoundException("Node not found: '" + nodePath + "'.");
        } catch (PathNotFoundException e) {
            throw new NotFoundException("Node not found: '" + nodePath + "'.");
        } catch (AccessDeniedException e) {
            throw new NotAllowedException("Access to node: '" + nodePath + "' not allowed.");
        } catch (RepositoryException e) {
            throw new GeneralErrorException(e);
        }
    }

    @GET
    @Path("/_uuid/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<NodeApiWrapper> nodeByUuid(@PathParam("workspace") String workspace,
            @PathParam("uuid") String uuid,
            @QueryParam("includeChildNodeTypes") String childNodeTypes) {
        try {
            checkAccess(workspace);
            NodeResolver nodeResolver = new NodeResolver(getSession(workspace));
            nodeResolver.setUuid(uuid);
            List<NodeApiWrapper> result = new ArrayList<NodeApiWrapper>();
            result.add(nodeResolver.getNode());
            return result;
        } catch (ItemNotFoundException e) {
            throw new NotFoundException("Node not found: UUID '" + uuid + "'.");
        } catch (PathNotFoundException e) {
            throw new NotFoundException("Node not found: UUID '" + uuid + "'.");
        } catch (AccessDeniedException e) {
            throw new NotAllowedException("Access to node UUID '" + uuid + "' not allowed.");
        } catch (RepositoryException e) {
            throw new GeneralErrorException(e);
        }
    }

    @GET
    @Path("/_query")
    @Produces(MediaType.APPLICATION_JSON)
    public List<NodeApiWrapper> query(@PathParam("workspace") String workspace,
            @QueryParam("query") String query,
            @QueryParam("language") String language,
            @QueryParam("limit") long limit,
            @QueryParam("offset") long offset) {
        checkAccess(workspace);
        NodeQueryManager queryManager;
        if (limit == 0 && offset == 0) {
            queryManager = new NodeQueryManager(workspace, query, language);
        } else if (limit > 0 && offset == 0) {
            queryManager = new NodeQueryManager(workspace, query, language, limit);
        } else {
            queryManager = new NodeQueryManager(workspace, query, language, limit, offset);
        }
        try {
            return queryManager.executeQuery();
        } catch (RepositoryException e) {
            throw new GeneralErrorException(e);
        }
    }

    @GET
    @Path("/_binary/{path: .+}")
    public Response getBinaryProperty(@PathParam("workspace") String workspace,
                @PathParam("path") String path, @QueryParam("property") String property) {
        try {
            Item item = getSession(workspace).getItem("/" + path);
            if (item != null && item.isNode()) {
                Node node = (Node) item;
                final InputStream stream = node.getProperty(property).getStream();
                StreamingOutput streamingOutput = new StreamingOutput() {
                    @Override
                    public void write(OutputStream output) throws IOException {
                        byte[] buffer = new byte[4096];
                        int read;
                        while ((read = stream.read(buffer)) != -1) {
                            output.write(buffer, 0, read);
                        }
                        stream.close();
                    }
                };
                Response.ResponseBuilder builder = Response.ok(streamingOutput);
                if ("mgnl:resource".equals(node.getPrimaryNodeType().getName())) {
                    builder.header("Content-Length", node.getProperty("size").getLong());
                    builder.header("Content-Type", node.getProperty("jcr:mimeType").getString());
                    builder.header("Last-Modified", node.getProperty("jcr:lastModified").
                            getDate().getTime());
                } else {
                    builder.header("Content-Type", "application/octet-stream");
                }
                return builder.build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (PathNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (RepositoryException e) {
            throw new GeneralErrorException(e);
        }
    }

    private void checkAccess(String workspace) {
        for (String forbiddenRepo : FORBIDDEN_WORKSPACES) {
            if (forbiddenRepo.equals(workspace)) {
                throw new NotAllowedException("Not allowed to access workspace '"
                        + forbiddenRepo + "'.");
            }
        }
        boolean inRole = false;
        for (String role : MgnlContextAdapter.getUserRoles()) {
            if (SINICUM_SERVER_ROLE.equals(role)) {
                inRole = true;
                break;
            }
        }
        if (!inRole) {
            throw new NotAllowedException("User must be a member of the 'sinicum-server' role.'");
        }
    }

    private Session getSession(String workspace) throws RepositoryException {
        return MgnlContextAdapter.getJcrSession(workspace);
    }
}
