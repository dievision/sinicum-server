package com.dievision.sinicum.server.jcr;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dievision.sinicum.server.mgnlAdapters.MgnlContextAdapter;

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
    private static final Logger logger = LoggerFactory.getLogger(WysiwygTemplateTranslator.class);


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
        if ("dms".equals(node.getSession().getWorkspace().getName())) {
            return DMS_PREFIX + node.getPath();
        } else {
            return node.getPath();
        }
    }
}
