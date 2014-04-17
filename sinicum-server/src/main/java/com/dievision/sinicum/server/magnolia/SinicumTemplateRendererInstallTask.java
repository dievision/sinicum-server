package com.dievision.sinicum.server.magnolia;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dievision.sinicum.server.mgnlAdapters.NodeTypeAdapter;
import com.dievision.sinicum.server.mgnlAdapters.TaskAdapterExecutionException;

/**
 *
 */
public class SinicumTemplateRendererInstallTask extends AbstractInstallTask {
    private static final String TEMPLATING_MODULE_PATH_44 =
            "/modules/templating/template-renderers";
    private static final String RENDERERS_FOLDER_NAME = "renderers";
    private static final String SINICUM_TEMPLATING_NODE_NAME = "sinicum";
    private static final String RENDERER_CLASS_NAME =
            "com.dievision.sinicum.server.magnolia.SinicumTemplateRenderer";
    private static final Logger logger =
            LoggerFactory.getLogger(SinicumTemplateRendererInstallTask.class);

    @Override
    public String getName() {
        return "Sinicum Template Renderer Install Task";
    }

    @Override
    public String getDescription() {
        return "Installs a dummy template renderer";
    }

    @Override
    public void execute(Session session) throws TaskAdapterExecutionException {
        try {
            if (isPost44(session)) {
                createSinicumTemplateHandler(session);
            } else {
                createSinicumTemplateHandler44(session);
            }
        } catch (RepositoryException e) {
            throw new TaskAdapterExecutionException(e.toString(), e);
        }
    }

    private void createSinicumTemplateHandler(Session session) throws RepositoryException {
        Item templatingItem = null;
        Item moduleBaseItem = null;
        try {
            moduleBaseItem = session.getItem(getModuleBasePath());
        } catch (PathNotFoundException e) {
            // nothing
        }
        if (moduleBaseItem != null && moduleBaseItem.isNode()) {
            Node moduleBase = (Node) moduleBaseItem;
            if (!moduleBase.hasNode(RENDERERS_FOLDER_NAME)) {
                templatingItem = moduleBase.addNode(RENDERERS_FOLDER_NAME,
                        NodeTypeAdapter.getContentType());
            } else {
                templatingItem = moduleBase.getNode(RENDERERS_FOLDER_NAME);
            }
        }
        if (templatingItem != null && templatingItem.isNode()) {
            Node templatingNode = (Node) templatingItem;
            if (!hasChildNode(templatingNode, SINICUM_TEMPLATING_NODE_NAME)) {
                Node sinicumTemplating = templatingNode.addNode(SINICUM_TEMPLATING_NODE_NAME,
                        NodeTypeAdapter.getContentNodeType());
                sinicumTemplating.setProperty("class",
                        "com.dievision.sinicum.server.magnolia.SinicumTemplateRenderer");
                sinicumTemplating.setProperty("type", "sinicum");
                session.save();
            }
        }
    }

    private void createSinicumTemplateHandler44(Session session) throws RepositoryException {
        Item templatingItem = null;
        try {
            templatingItem = session.getItem(TEMPLATING_MODULE_PATH_44);
        } catch (PathNotFoundException e) {
            // nothing
        }
        if (templatingItem != null && templatingItem.isNode()) {
            Node templatingNode = (Node) templatingItem;
            if (!hasChildNode(templatingNode, SINICUM_TEMPLATING_NODE_NAME)) {
                Node sinicumTemplating = templatingNode.addNode(SINICUM_TEMPLATING_NODE_NAME,
                        NodeTypeAdapter.getContentNodeType());
                sinicumTemplating.setProperty("renderer", RENDERER_CLASS_NAME);
                sinicumTemplating.setProperty("type", "sinicum");
                session.save();
            }
        }
    }

    private boolean isPost44(Session session) throws RepositoryException {
        boolean post44 = true;
        try {
            session.getItem(TEMPLATING_MODULE_PATH_44);
            post44 = false;
        } catch (PathNotFoundException e) {
            // nothing
        }
        return post44;
    }
}
