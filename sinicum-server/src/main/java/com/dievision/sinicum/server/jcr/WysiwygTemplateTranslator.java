package com.dievision.sinicum.server.jcr;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dievision.sinicum.server.mgnlAdapters.MgnlContextAdapter;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.NodeIterator;


/**
 * <p>Translates the Freemarker-based links to other nodes created by the WYSIWYG editor to
 * real paths.</p>
 *
 * <p>The expexted format is someting like:</p>
 *
 * <p><tt>${link:{uuid:{2f1f9b46-2edf-4548-bdcc-ece9a7b03d43},repository:{website},
 * handle:{/path/to/node},nodeData:{},extension:{html}}}</tt></p>
 */
public class WysiwygTemplateTranslator {
    private static final Pattern UUID_PATTERN = Pattern.compile(
            "\\$\\{link:.+?uuid:\\{(.*?)\\}.+?repository:\\{(.*?)\\}.*?\\}\\}\\}");
    private static final String DMS_PREFIX = "/dmsfiles/default";
    private static final String DAM_PREFIX = "/damfiles/default";
    private static final String JCR_CONTENT_PATH = "jcr:content";
    private static final String EXTENSION_PROP = "extension";
    private static final String FINGERPRINT_VERSION = "2";
    private static final String FINGERPRINT_ALGORITHM = "MD5";
    private static final Logger logger = LoggerFactory.getLogger(WysiwygTemplateTranslator.class);
    private static final ThreadLocal<ArrayList<String>> MULTISITE_NODES =
            new ThreadLocal<ArrayList<String>>() {
                @Override
                protected ArrayList<String> initialValue() {
                    ArrayList<String> list = new ArrayList<String>();
                    Session session = null;
                    try {
                        session = MgnlContextAdapter.getJcrSession("multisite");
                        NodeIterator it = session.getRootNode().getNodes();
                        while (it.hasNext()) {
                            Node node = it.nextNode();
                            if (node.hasProperty("root_node")) {
                                list.add(node.getProperty("root_node").getString());
                            }
                        }
                    } catch (RepositoryException e) {
                        e.printStackTrace();
                    }
                    return list;
                }
            };


    public String translate(String source) {
        Matcher matcher = UUID_PATTERN.matcher(source);
        String result = source;
        while (matcher.find()) {
            String uuid = matcher.group(1);
            String repository = matcher.group(2);
            String replacementPath = findReplacementPath(uuid, repository);
            result = matcher.replaceFirst(replacementPath);
            matcher = UUID_PATTERN.matcher(result);
        }
        return result;
    }

    private String findReplacementPath(String uuid, String repository) {
        String result = "#";
        try {
            Session session = MgnlContextAdapter.getJcrSession(repository);
            Node node = session.getNodeByUUID(uuid);
            result = workspaceAdjustedPath(node);
        } catch (ItemNotFoundException e) {
            // nothing
        } catch (RepositoryException e) {
            logger.error("Debug: " + e.toString());
        }
        return result;
    }

    private String workspaceAdjustedPath(Node node) throws RepositoryException {
        if ("dam".equals(node.getSession().getWorkspace().getName())) {
            return DAM_PREFIX + getDamPath(node);
        } else if ("dms".equals(node.getSession().getWorkspace().getName())) {
            return DMS_PREFIX + node.getPath();
        } else {
            return multisiteAwarePath(node.getPath());
        }
    }

    private String getDamPath(Node node) throws RepositoryException {
        String path = node.getPath();
        if (node.hasNode(JCR_CONTENT_PATH)) {
            Node jcrContent = node.getNode(JCR_CONTENT_PATH);
            String fingerprint = getFingerprint(node, jcrContent);
            if (fingerprint != null) {
                path += "-" + fingerprint;
            }
            if (jcrContent.hasProperty(EXTENSION_PROP)
                    && !"".equals(jcrContent.getProperty(EXTENSION_PROP))) {
                path += "." + jcrContent.getProperty(EXTENSION_PROP).getString();
            }
        }
        return path;
    }

    private String getFingerprint(Node node, Node jcrContent) throws RepositoryException {
        String fingerprint = null;
        String lastModified = null;
        if (jcrContent.hasProperty("mgnl:lastModified")) {
            lastModified = "mgnl:lastModified";
        } else {
            lastModified = "jcr:lastModified";
        }
        try {
            String identifier = FINGERPRINT_VERSION + "-" + node.getPath() + "-" + node.getUUID()
                    + jcrContent.getProperty(lastModified).getString() + "-"
                    + jcrContent.getProperty(lastModified + "By").getString() + "-"
                    + jcrContent.getProperty("size").getString();
            fingerprint = DigestUtils.md5Hex(identifier);
        } catch (RepositoryException e) {
            logger.error("Could not create fingerprint: " + e.toString());
        }
        return fingerprint;
    }

    private String multisiteAwarePath(String path) throws RepositoryException {
        for (String rootNode : MULTISITE_NODES.get()) {
            path = path.replaceAll(rootNode, "");
        }
        return path;
    }
}
