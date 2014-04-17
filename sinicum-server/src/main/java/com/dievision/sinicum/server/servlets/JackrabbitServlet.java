package com.dievision.sinicum.server.servlets;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.jackrabbit.api.JackrabbitNodeTypeManager;
import org.apache.jackrabbit.api.JackrabbitRepository;
import org.apache.jackrabbit.core.RepositoryImpl;
import org.apache.jackrabbit.core.config.RepositoryConfig;

import com.dievision.sinicum.server.jackrabbit.JackrabbitManager;


/**
 *
 */
public class JackrabbitServlet extends HttpServlet {
    private JackrabbitRepository repository;

    @Override
    public void init() throws ServletException {
        File home = new File(getInitParameter("repository.home"), "jackrabbit-repository");
        if (!home.exists()) {
            log("Creating repostitory home");
            home.mkdirs();
        }
        File config = new File(getInitParameter("repository.config"),
                new File(home, "repository.xml").getPath());
        boolean createNew = false;
        if (!config.exists()) {
            log("Creating default repository configuration");
            createDefaultConfiguration(config);
            createNew = true;
        }
        try {
            repository = RepositoryImpl.create(
                    RepositoryConfig.create(config.toURI(), home.getPath()));
            if (createNew) {
                registerNodeTypes(repository);
            }
            JackrabbitManager.registerRepository(repository);
        } catch (RepositoryException e) {
            throw new ServletException("Failed to initialize repository: " + e.toString(), e);
        } catch (IOException e) {
            throw new ServletException("Failed to initialize repository: " + e.toString(), e);
        }
        super.init();
    }

    private void registerNodeTypes(JackrabbitRepository repository)
        throws RepositoryException, IOException {
        Session session = repository.login(new SimpleCredentials(
                "admin", "admin".toCharArray()));
        JackrabbitNodeTypeManager ntm = (JackrabbitNodeTypeManager) session.getWorkspace().
                getNodeTypeManager();
        InputStream input = null;
        try {
            input = getClass().getResourceAsStream("/mgnl-nodetypes/magnolia-nodetypes.xml");
            ntm.registerNodeTypes(input, JackrabbitNodeTypeManager.TEXT_XML);
        } finally {
            if (input != null) {
                input.close();
            }
        }

    }

    @Override
    public void destroy() {
        super.destroy();
        repository.shutdown();
    }

    private void createDefaultConfiguration(File config) throws ServletException {
        try {
            OutputStream out = new FileOutputStream(config);
            try {
                InputStream input = RepositoryImpl.class.getResourceAsStream("repository.xml");
                try {
                    byte[] buffer = new byte[4096];
                    int n = input.read(buffer);
                    while (n != -1) {
                        out.write(buffer, 0, n);
                        n = input.read(buffer);
                    }
                } finally {
                    input.close();
                }
            } finally {
                out.close();
            }
        } catch (IOException e) {
            throw new ServletException("Failed to cretae default configuration: " + config, e);
        }
    }
}
