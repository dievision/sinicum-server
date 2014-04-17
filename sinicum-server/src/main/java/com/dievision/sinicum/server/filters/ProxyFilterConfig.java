package com.dievision.sinicum.server.filters;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ProxyFilterConfig {
    private static ProxyFilterConfig instance = null;
    private Pattern proxyPattern;
    private URI proxyTargetUri;
    private static final String DEFAULT_PATH_PATTERN = "/.*";
    private static final String DEFAULT_PROXY_TARGET_URI = "http://localhost:3000";
    private static final Logger logger = LoggerFactory.getLogger(ProxyFilterConfig.class);

    private ProxyFilterConfig() {
        // nothing
    }

    public boolean matchesPath(String path) {
        if (path == null) {
            return false;
        } else {
            return proxyPattern.matcher(path).matches();
        }
    }

    public URI getProxyTargetUri() {
        return proxyTargetUri;
    }

    public void setProxyTargetUri(URI proxyTargetUri) {
        this.proxyTargetUri = proxyTargetUri;
    }

    public void setProxyPattern(String pattern) {
        proxyPattern = Pattern.compile(pattern);
    }

    public static ProxyFilterConfig getInstance() {
        if (instance == null) {
            synchronized (ProxyFilterConfig.class) {
                if (instance == null) {
                    ProxyFilterConfig proxyFilterConfig = new ProxyFilterConfig();
                    proxyFilterConfig.setProxyPattern(DEFAULT_PATH_PATTERN);
                    try {
                        proxyFilterConfig.setProxyTargetUri(new URI(DEFAULT_PROXY_TARGET_URI));
                    } catch (URISyntaxException e) {
                        logger.error("Error setting sinicum-server default proxy target: "
                                + e.toString());
                    }
                    instance = proxyFilterConfig;
                }
            }
        }
        return instance;
    }

}
