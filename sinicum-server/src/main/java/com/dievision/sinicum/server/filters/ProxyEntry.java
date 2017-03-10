package com.dievision.sinicum.server.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

public class ProxyEntry {
    private Pattern proxyPattern;
    private URI proxyTargetUri;
    private static final String DEFAULT_PATH_PATTERN = "/.*";
    private static final String DEFAULT_PROXY_TARGET_URI = "http://localhost:3000";
    private static final Logger logger = LoggerFactory.getLogger(ProxyEntry.class);

    public ProxyEntry() {
        this(DEFAULT_PATH_PATTERN, DEFAULT_PROXY_TARGET_URI);
    }

    public ProxyEntry(String proxyPattern, String proxyTargetUri) {
        this.proxyPattern = Pattern.compile(proxyPattern);
        try {
            this.proxyTargetUri = new URI(proxyTargetUri);
        } catch (URISyntaxException e) {
            logger.error("Error setting sinicum-server default proxy target: "
                    + e.toString());
        }
    }

    public boolean matchesPath(String path) {
        if (path == null) {
            return false;
        } else {
            return proxyPattern.matcher(path).matches();
        }
    }

    public Pattern getProxyPattern() {
        return proxyPattern;
    }

    public void setProxyPattern(String proxyPattern) {
        this.proxyPattern = Pattern.compile(proxyPattern);
    }

    public URI getProxyTargetUri() {
        return proxyTargetUri;
    }

    public void setProxyTargetUri(URI proxyTargetUri) {
        this.proxyTargetUri = proxyTargetUri;
    }
}
