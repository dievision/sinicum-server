package com.dievision.sinicum.server.magnolia;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.container.filter.GZIPContentEncodingFilter;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.Task;
import info.magnolia.module.delta.TaskExecutionException;

import com.dievision.sinicum.server.jaxrs.filters.LoginFilter;
import com.dievision.sinicum.server.jaxrs.filters.PrettyPrintFilter;
import com.dievision.sinicum.server.jaxrs.filters.TimingResponseFilter;

public class SinicumRestServletInstallTask implements Task {
    private Session session;
    private static final String FILTER_LOCATION_PARENT = "/server/filters";
    private static final String FILTER_NODE_NAME = "sinicumRest";
    private static final String OLD_FILTER_LOCATION =
            "/server/filters/servlets/Sinicum REST Servlet";
    private static final String AFTER_FILTER_NODE = "activation";
    private static final Logger logger =
            LoggerFactory.getLogger(SinicumRestServletInstallTask.class);

    @Override
    public String getName() {
        return "Sinicum Rest Servlet Installer";
    }

    @Override
    public String getDescription() {
        return "Installs the Sinicum REST servlet";
    }

    @Override
    public void execute(InstallContext installContext) throws TaskExecutionException {
        try {
            this.session = installContext.getConfigJCRSession();
            installFilter();
        } catch (RepositoryException e) {
            throw new TaskExecutionException(e.toString(), e);
        }
    }

    private void installFilter() throws RepositoryException {
        Node filterNode = findOrCreateFilterNode();
        if (filterNode != null) {
            Node servletNode = filterNode.addNode("Sinicum REST Servlet", "mgnl:contentNode");
            servletNode.setProperty("class", "info.magnolia.cms.filters.ServletDispatchingFilter");
            servletNode.setProperty("comment", "The filter used by the sinicum-server module.");
            servletNode.setProperty("enabled", true);
            servletNode.setProperty("servletClass", ServletContainer.class.getName());
            servletNode.setProperty("servletName", "Sinicum Rest Servlet");
            createMappings(servletNode);
            createParameters(servletNode);
            session.save();
            orderNode(servletNode.getParent());
            session.save();
        }
    }

    private void orderNode(Node servletNode) throws RepositoryException {
        Node filters = (Node) session.getItem(FILTER_LOCATION_PARENT);
        if (filters.hasNode(AFTER_FILTER_NODE)) {
            filters.orderBefore(servletNode.getName(),
                    filters.getNode(AFTER_FILTER_NODE).getName());
        } else {
            logger.error("Could not order Sinicum Server filter: Filter node "
                    + AFTER_FILTER_NODE + " not found.");
        }
    }

    private void createMappings(Node servletNode) throws RepositoryException {
        Node mappings = servletNode.addNode("mappings", "mgnl:contentNode");
        Node sinicumRest = mappings.addNode("sinicum-rest", "mgnl:contentNode");
        sinicumRest.setProperty("pattern", "/sinicum-rest/*");
    }

    private void createParameters(Node servletNode) throws RepositoryException {
        Node parameters = servletNode.addNode("parameters", "mgnl:contentNode");
        parameters.setProperty("com.sun.jersey.config.property.packages",
                "com.dievision.sinicum.server.resources");
        parameters.setProperty("com.sun.jersey.spi.container.ContainerRequestFilters",
                TimingResponseFilter.class.getName() + ";"
                        + LoginFilter.class.getName() + ";"
                        + PrettyPrintFilter.class.getName() + ";"
                        + GZIPContentEncodingFilter.class.getName()
        );
        parameters.setProperty("com.sun.jersey.spi.container.ContainerResponseFilters",
                GZIPContentEncodingFilter.class.getName() + ";"
                        + TimingResponseFilter.class.getName());
    }

    private Node findOrCreateFilterNode() {
        Node filterNode = null;
        try {
            Node filters = (Node) session.getItem(FILTER_LOCATION_PARENT);
            if (filters.hasNode(FILTER_NODE_NAME)) {
                logger.info("Sinicum Rest Filter not installed, Node " + FILTER_NODE_NAME
                        + " already exists");
            } else {
                filterNode = filters.addNode(FILTER_NODE_NAME, "mgnl:content");
            }
        } catch (ClassCastException e) {
            logger.error("Could not find " + FILTER_LOCATION_PARENT + " node: " + e.toString());
        } catch (RepositoryException e) {
            logger.error("Could not find " + FILTER_LOCATION_PARENT + " node: " + e.toString());
        }
        return filterNode;
    }
}
