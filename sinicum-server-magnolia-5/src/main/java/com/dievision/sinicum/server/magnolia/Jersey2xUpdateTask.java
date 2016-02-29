package com.dievision.sinicum.server.magnolia;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.Task;
import info.magnolia.module.delta.TaskExecutionException;

public class Jersey2xUpdateTask implements Task {
    private Session session;
    private static final String CONFIG_PATH = "/server/filters/sinicumRest";
    private static final Logger logger = LoggerFactory.getLogger(Jersey2xUpdateTask.class);

    public String getName() {
        return "Jersey 2.x Update Task";
    }

    public String getDescription() {
        return getName();
    }

    public void execute(InstallContext installContext) throws TaskExecutionException {
        try {
            session = installContext.getConfigJCRSession();
            Node configNode = findServletConfigNode();
            if (configNode != null) {
                updateServletClass(configNode);
                updateServletParameters(configNode);
                session.save();
            }
        } catch (RepositoryException e) {
            throw new TaskExecutionException(e.toString(), e);
        }
    }

    private void updateServletParameters(Node configNode) throws RepositoryException {
        if (configNode.hasNode("parameters")) {
            Node parameters = configNode.getNode("parameters");
            parameters.remove();
        }
        Node parameters = configNode.addNode("parameters", "mgnl:contentNode");
        parameters.setProperty("javax.ws.rs.Application",
                "com.dievision.sinicum.server.SinciumServerApplication");
    }

    private void updateServletClass(Node configNode) throws RepositoryException {
        configNode.setProperty("servletClass", ServletContainer.class.getName());
    }

    private Node findServletConfigNode() throws RepositoryException {
        Node configRoot = session.getNode(CONFIG_PATH);
        NodeIterator iterator = configRoot.getNodes();
        if (iterator.hasNext()) {
            return iterator.nextNode();
        } else {
            return null;
        }
    }
}
