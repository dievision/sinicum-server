package com.dievision.sinicum.server.filters;

/**
 * Copyright MITRE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.dievision.sinicum.server.mgnlAdapters.AggregationStateAdapter;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.AbortableHttpRequest;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.message.HeaderGroup;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpCookie;
import java.util.BitSet;
import java.util.Enumeration;
import java.util.Formatter;
import java.util.regex.Pattern;

/**
 * An HTTP reverse proxy/gateway servlet. It is designed to be extended for customization if
 * desired. Most of the work is handled by
 * <a href="http://hc.apache.org/httpcomponents-client-ga/">Apache HttpClient</a>.
 * <p>
 *     There are alternatives to a servlet based proxy such as Apache mod_proxy if
 *     that is available to you. However this servlet is easily customizable by Java, secure-able by
 *     your web application's security (e.g. spring-security), portable across servlet engines, and
 *     is embeddable into another web application.
 * </p>
 * <p>
 *     Inspiration: http://httpd.apache.org/docs/2.0/mod/mod_proxy.html
 * </p>
 *
 * @author David Smiley dsmiley@mitre.org>
 */
public class ProxyFilter {
    private static final Pattern[] MGNL_PATHS = {Pattern.compile("^/$"),
            Pattern.compile("^.magnolia/.*"),
            Pattern.compile("^/.resources/.*"),
            Pattern.compile("^/dataModule/.*")
    };
    private AggregationStateAdapter aggregationStateAdapter;

    private static final Logger logger = LoggerFactory.getLogger(ProxyFilter.class);


    public ProxyFilter(AggregationStateAdapter aggregationStateAdapter) {
        this.aggregationStateAdapter = aggregationStateAdapter;
        HttpParams hcParams = new BasicHttpParams();
        hcParams.setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);
    }

    public void doFilter(HttpServletRequest request, HttpServletResponse response,
                         FilterChain filterChain) throws ServletException, IOException {
        String proxyPath = request.getRequestURI();
        boolean performProxying = ProxyFilterConfig.getInstance().matchesPath(proxyPath);
        if (performProxying) {
            for (Pattern pattern : MGNL_PATHS) {
                if (pattern.matcher(proxyPath).matches()) {
                    performProxying = false;
                    break;
                }
            }
        }
        if (performProxying) {
            performProxyRequest(request, response);
        } else {
            filterChain.doFilter(request, response);
            return;
        }
    }

    private void performProxyRequest(HttpServletRequest servletRequest,
                                     HttpServletResponse servletResponse)
        throws IOException, ServletException {
        // Make the Request
        // note: we won't transfer the protocol version because I'm not sure it would truly be
        // compatible
        String method = servletRequest.getMethod();
        String proxyRequestUri = rewriteUrlFromRequest(servletRequest);
        HttpRequest proxyRequest;
        //spec: RFC 2616, sec 4.3: either these two headers signal that there is a message body.
        if (servletRequest.getHeader(HttpHeaders.CONTENT_LENGTH) != null
                || servletRequest.getHeader(HttpHeaders.TRANSFER_ENCODING) != null) {
            HttpEntityEnclosingRequest eProxyRequest =
                    new BasicHttpEntityEnclosingRequest(method, proxyRequestUri);
            // Add the input entity (streamed)
            // note: we don't bother ensuring we close the servletInputStream since the container
            // handles it
            eProxyRequest.setEntity(new InputStreamEntity(servletRequest.getInputStream(),
                    servletRequest.getContentLength()));
            proxyRequest = eProxyRequest;
        } else {
            proxyRequest = new BasicHttpRequest(method, proxyRequestUri);
        }

        copyRequestHeaders(servletRequest, proxyRequest);
        addProtocolHeaders(proxyRequest);
        addMagnoliaHeaders(proxyRequest);

        HttpClient proxyClient = createNewClient();
        try {
            // Execute the request
            HttpResponse proxyResponse =
                    proxyClient.execute(URIUtils.extractHost(
                            ProxyFilterConfig.getInstance().getProxyTargetUri()), proxyRequest);

            // Process the response


            // Pass the response code. This method with the "reason phrase" is deprecated but it's
            // the only way to pass the reason along too.
            int statusCode = proxyResponse.getStatusLine().getStatusCode();

            // noinspection deprecation
            servletResponse.setStatus(statusCode, proxyResponse.getStatusLine().getReasonPhrase());

            copyResponseHeaders(proxyResponse, servletRequest, servletResponse);

            if (statusCode == HttpServletResponse.SC_NOT_MODIFIED) {
                // 304 needs special handling.  See:
                // http://www.ics.uci.edu/pub/ietf/http/rfc1945.html#Code304
                // Don't send body entity/content!
                servletResponse.setIntHeader(HttpHeaders.CONTENT_LENGTH, 0);
            } else {
                // Send the content to the client
                copyResponseEntity(proxyResponse, servletResponse, proxyRequest, servletRequest);
            }
        } catch (Exception e) {
            //abort request, according to best practice with HttpClient
            if (proxyRequest instanceof AbortableHttpRequest) {
                AbortableHttpRequest abortableHttpRequest = (AbortableHttpRequest) proxyRequest;
                abortableHttpRequest.abort();
            }
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            if (e instanceof ServletException) {
                throw (ServletException) e;
            }
            if (e instanceof IOException) {
                throw (IOException) e;
            }
            throw new RuntimeException(e);
        } finally {
            proxyClient.getConnectionManager().shutdown();
        }
    }

    private HttpClient createNewClient() {
        HttpParams hcParams = new BasicHttpParams();
        hcParams.setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);
        HttpClient client = new DefaultHttpClient(hcParams);
        return client;
    }

    private void addProtocolHeaders(HttpRequest proxyRequest) {
        if (isSecureProxyRequest(proxyRequest)) {
            proxyRequest.addHeader("X-Forwarded-Proto", "https");
        }
    }


    private void addMagnoliaHeaders(HttpRequest proxyRequest) {
        proxyRequest.addHeader("X-Mgnl-Admin", "true");
        proxyRequest.addHeader("X-Mgnl-Preview",
                Boolean.toString(aggregationStateAdapter.isPreviewMode()));
    }

    private boolean isSecureProxyRequest(HttpRequest proxyRequest) {
        return proxyRequest.getRequestLine() != null
                && proxyRequest.getRequestLine().getUri() != null
                && proxyRequest.getRequestLine().getUri().startsWith("https://");
    }

    private void closeQuietly(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * Copy request headers from the servlet client to the proxy request.
     */
    private void copyRequestHeaders(HttpServletRequest servletRequest, HttpRequest proxyRequest) {
        // Get an Enumeration of all of the header names sent by the client
        Enumeration enumerationOfHeaderNames = servletRequest.getHeaderNames();
        while (enumerationOfHeaderNames.hasMoreElements()) {
            String headerName = (String) enumerationOfHeaderNames.nextElement();
            //Instead the content-length is effectively set via InputStreamEntity
            if (headerName.equalsIgnoreCase(HttpHeaders.CONTENT_LENGTH)) {
                continue;
            }
            if (HOP_BY_HOP_HEADERS.containsHeader(headerName)) {
                continue;
            }
            if ("X-Forwarded-Proto".equalsIgnoreCase(headerName)) {
                continue;
            }
            // As per the Java Servlet API 2.5 documentation:
            //     Some headers, such as Accept-Language can be sent by clients
            //     as several headers each with a different value rather than
            //     sending the header as a comma separated list.
            // Thus, we get an Enumeration of the header values sent by the client
            Enumeration headers = servletRequest.getHeaders(headerName);
            while (headers.hasMoreElements()) {
                String headerValue = (String) headers.nextElement();
                // In case the proxy host is running multiple virtual servers,
                // rewrite the Host header to ensure that we get content from
                // the correct virtual server
                if (headerName.equalsIgnoreCase(HttpHeaders.HOST)) {
                    HttpHost host = URIUtils.extractHost(
                            ProxyFilterConfig.getInstance().getProxyTargetUri());
                    headerValue = host.getHostName();
                    if (host.getPort() != -1) {
                        headerValue += ":" + host.getPort();
                    }
                } else if (headerName.equalsIgnoreCase(org.apache.http.cookie.SM.COOKIE)) {
                    headerValue = getRealCookie(headerValue);
                }
                proxyRequest.addHeader(headerName, headerValue);
            }
        }
    }

    /** Copy proxied response headers back to the servlet client. */
    protected void copyResponseHeaders(HttpResponse proxyResponse,
                                       HttpServletRequest servletRequest,
                                       HttpServletResponse servletResponse) {
        for (Header header : proxyResponse.getAllHeaders()) {
            copyResponseHeader(servletRequest, servletResponse, header);
        }
    }

    /** Copy a proxied response header back to the servlet client.
     * This is easily overwritten to filter out certain headers if desired.
     */
    protected void copyResponseHeader(HttpServletRequest servletRequest,
                                      HttpServletResponse servletResponse, Header header) {
        String headerName = header.getName();
        if (HOP_BY_HOP_HEADERS.containsHeader(headerName)) {
            return;
        }
        String headerValue = header.getValue();
        if (headerName.equalsIgnoreCase(org.apache.http.cookie.SM.SET_COOKIE)
                || headerName.equalsIgnoreCase(org.apache.http.cookie.SM.SET_COOKIE2)) {
            copyProxyCookie(servletRequest, servletResponse, headerValue);
        } else if (headerName.equalsIgnoreCase(HttpHeaders.LOCATION)) {
            // LOCATION Header may have to be rewritten.
            servletResponse.addHeader(headerName,
                    rewriteUrlFromResponse(servletRequest, headerValue));
        } else {
            servletResponse.addHeader(headerName, headerValue);
        }
    }

    /**
     * Copy cookie from the proxy to the servlet client.
     * Replaces cookie path to local path and renames cookie to avoid collisions.
     */
    protected void copyProxyCookie(HttpServletRequest servletRequest,
                                   HttpServletResponse servletResponse, String headerValue) {
        for (HttpCookie cookie : HttpCookie.parse(headerValue)) {
            //set cookie name prefixed w/ a proxy value so it won't collide w/ other cookies
            String proxyCookieName = getCookieNamePrefix(cookie.getName()) + cookie.getName();
            Cookie servletCookie = new Cookie(proxyCookieName, cookie.getValue());
            servletCookie.setComment(cookie.getComment());
            servletCookie.setMaxAge(1200); // 20 minutes
            servletCookie.setPath(cookie.getPath());
            // don't set cookie domain
            servletCookie.setSecure(cookie.getSecure());
            servletCookie.setVersion(cookie.getVersion());
            servletResponse.addCookie(servletCookie);
        }
    }

    /**
     * Take any client cookies that were originally from the proxy and prepare them to send to the
     * proxy.  This relies on cookie headers being set correctly according to RFC 6265 Sec 5.4.
     * This also blocks any local cookies from being sent to the proxy.
     */
    protected String getRealCookie(String cookieValue) {
        StringBuilder escapedCookie = new StringBuilder();
        String[] cookies = cookieValue.split("[;,]");
        for (String cookie : cookies) {
            String[] cookieSplit = cookie.split("=");
            if (cookieSplit.length == 2) {
                String cookieName = cookieSplit[0].trim();
                if (cookieName.startsWith(getCookieNamePrefix(cookieName))) {
                    cookieName = cookieName.substring(getCookieNamePrefix(cookieName).length());
                    if (escapedCookie.length() > 0) {
                        escapedCookie.append("; ");
                    }
                    escapedCookie.append(cookieName).append("=").append(cookieSplit[1].trim());
                }
            }
        }
        return escapedCookie.toString();
    }

    /** The string prefixing rewritten cookies. */
    protected String getCookieNamePrefix(String name) {
        return "!Proxy!Sinicum-Server";
    }

    /** Copy response body data (the entity) from the proxy to the servlet client. */
    protected void copyResponseEntity(HttpResponse proxyResponse,
                                      HttpServletResponse servletResponse,
                                      HttpRequest proxyRequest, HttpServletRequest servletRequest)
        throws IOException {
        HttpEntity entity = proxyResponse.getEntity();
        if (entity != null) {
            OutputStream servletOutputStream = servletResponse.getOutputStream();
            entity.writeTo(servletOutputStream);
        }
    }

    private String rewriteUrlFromRequest(HttpServletRequest request) {
        StringBuilder uri = new StringBuilder(500);
        uri.append(ProxyFilterConfig.getInstance().getProxyTargetUri().toString());
        // Handle the path given to the servlet
        if (request.getRequestURI() != null) { //ex: /my/path.html
            uri.append(request.getRequestURI());
        }
        // Handle the query string
        String queryString =
                request.getQueryString(); //ex:(following '?'): name=value&foo=bar#fragment
        if (queryString != null && queryString.length() > 0) {
            uri.append('?');
            int fragIdx = queryString.indexOf('#');
            String queryNoFrag = (fragIdx < 0 ? queryString : queryString.substring(0, fragIdx));
            uri.append(encodeUriQuery(queryNoFrag));
            if (fragIdx >= 0) {
                uri.append('#');
                uri.append(encodeUriQuery(queryString.substring(fragIdx + 1)));
            }
        }
        return uri.toString();
    }

    private String rewriteUrlFromResponse(HttpServletRequest request, String theUrl) {
        //TODO document example paths
        if (theUrl.startsWith(ProxyFilterConfig.getInstance().getProxyTargetUri().toString())) {
            String curUrl = request.getRequestURL().toString(); //no query
            String pathInfo = request.getRequestURI();
            if (pathInfo != null) {
                assert curUrl.endsWith(pathInfo);
                //take pathInfo off
                curUrl = curUrl.substring(0, curUrl.length() - pathInfo.length());
            }
            theUrl = curUrl + theUrl.substring(ProxyFilterConfig.getInstance()
                    .getProxyTargetUri().toString().length());
        }
        return theUrl;
    }

    /**
     * These are the "hop-by-hop" headers that should not be copied.
     * http://www.w3.org/Protocols/rfc2616/rfc2616-sec13.html
     * I use an HttpClient HeaderGroup class instead of Set<String> because this approach does case
     * insensitive lookup faster.
     */
    private static final HeaderGroup HOP_BY_HOP_HEADERS;

    static {
        HOP_BY_HOP_HEADERS = new HeaderGroup();
        String[] headers = new String[]{
            "Connection", "Keep-Alive", "Proxy-Authenticate", "Proxy-Authorization",
            "TE", "Trailers", "Transfer-Encoding", "Upgrade"};
        for (String header : headers) {
            HOP_BY_HOP_HEADERS.addHeader(new BasicHeader(header, null));
        }
    }

    /**
     * <p>Encodes characters in the query or fragment part of the URI.
     * <p/>
     * <p>Unfortunately, an incoming URI sometimes has characters disallowed by the spec.
     * HttpClient insists that the outgoing proxied request has a valid URI because it uses Java's
     * URI. To be more forgiving, we must escape the problematic characters.  See the URI
     * class for the spec.
     *
     * @param in example: name=value&foo=bar#fragment
     */
    static CharSequence encodeUriQuery(CharSequence in) {
        //Note that I can't simply use URI.java to encode because it will escape pre-existing
        // escaped things.
        StringBuilder outBuf = null;
        Formatter formatter = null;
        for (int i = 0; i < in.length(); i++) {
            char c = in.charAt(i);
            boolean escape = true;
            if (c < 128) {
                if (ASCII_QUERY_CHARS.get((int) c)) {
                    escape = false;
                }
            } else if (!Character.isISOControl(c) && !Character.isSpaceChar(c)) { //not-ascii
                escape = false;
            }
            if (!escape) {
                if (outBuf != null) {
                    outBuf.append(c);
                }
            } else {
                //escape
                if (outBuf == null) {
                    outBuf = new StringBuilder(in.length() + 5 * 3);
                    outBuf.append(in, 0, i);
                    formatter = new Formatter(outBuf);
                }
                //leading %, 0 padded, width 2, capital hex
                formatter.format("%%%02X", (int) c); //TODO
            }
        }
        return outBuf != null ? outBuf : in;
    }


    static final BitSet ASCII_QUERY_CHARS;

    static {
        char[] cUnreserved = "_-!.~'()*".toCharArray(); //plus alphanum
        char[] cPunct = ",;:$&+=".toCharArray();
        char[] cReserved = "?/[]@".toCharArray(); //plus punct

        ASCII_QUERY_CHARS = new BitSet(128);
        for (char c = 'a'; c <= 'z'; c++) {
            ASCII_QUERY_CHARS.set((int) c);
        }
        for (char c = 'A'; c <= 'Z'; c++) {
            ASCII_QUERY_CHARS.set((int) c);
        }
        for (char c = '0'; c <= '9'; c++) {
            ASCII_QUERY_CHARS.set((int) c);
        }
        for (char c : cUnreserved) {
            ASCII_QUERY_CHARS.set((int) c);
        }
        for (char c : cPunct) {
            ASCII_QUERY_CHARS.set((int) c);
        }
        for (char c : cReserved) {
            ASCII_QUERY_CHARS.set((int) c);
        }

        ASCII_QUERY_CHARS.set((int) '%'); //leave existing percent escapes in place
    }
}
