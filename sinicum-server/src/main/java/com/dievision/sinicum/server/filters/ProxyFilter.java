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
import org.apache.http.util.EntityUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.BitSet;
import java.util.Enumeration;
import java.util.Formatter;
import java.util.regex.Pattern;

import com.dievision.sinicum.server.mgnlAdapters.AggregationStateAdapter;

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

    private void performProxyRequest(HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException {
        // Make the Request
        // note: we won't transfer the protocol version because I'm not sure it would truly be
        // compatible
        String method = request.getMethod();
        String proxyRequestUri = rewriteUrlFromRequest(request);
        HttpRequest proxyRequest;
        //spec: RFC 2616, sec 4.3: either these two headers signal that there is a message body.
        if (request.getHeader(HttpHeaders.CONTENT_LENGTH) != null
                || request.getHeader(HttpHeaders.TRANSFER_ENCODING) != null) {
            HttpEntityEnclosingRequest eProxyRequest =
                    new BasicHttpEntityEnclosingRequest(method, proxyRequestUri);
            // Add the input entity (streamed)
            // note: we don't bother ensuring we close the servletInputStream since the container
            // handles it
            eProxyRequest.setEntity(new InputStreamEntity(request.getInputStream(),
                    request.getContentLength()));
            proxyRequest = eProxyRequest;
        } else {
            proxyRequest = new BasicHttpRequest(method, proxyRequestUri);
        }

        copyRequestHeaders(request, proxyRequest);
        setMagnoliaHeaders(proxyRequest);

        HttpClient proxyClient = createNewClient();
        try {
            // Execute the request
            HttpResponse proxyResponse =
                    proxyClient.execute(URIUtils.extractHost(
                            ProxyFilterConfig.getInstance().getProxyTargetUri()), proxyRequest);

            // Process the response
            int statusCode = proxyResponse.getStatusLine().getStatusCode();

            if (doResponseRedirectOrNotModifiedLogic(request, response, proxyResponse,
                    statusCode)) {
                //just to be sure, but is probably a no-op
                EntityUtils.consume(proxyResponse.getEntity());
                return;
            }

            // Pass the response code. This method with the "reason phrase" is deprecated but it's
            // the only way to pass the reason along too.
            // noinspection deprecation
            response.sendError(statusCode, proxyResponse.getStatusLine().getReasonPhrase());

            copyResponseHeaders(proxyResponse, response);

            // Send the content to the client
            copyResponseEntity(proxyResponse, response);

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

    private void setMagnoliaHeaders(HttpRequest proxyRequest) {
        proxyRequest.addHeader("X-Mgnl-Admin", "true");
        proxyRequest.addHeader("X-Mgnl-Preview",
                Boolean.toString(aggregationStateAdapter.isPreviewMode()));
    }

    private boolean doResponseRedirectOrNotModifiedLogic(HttpServletRequest servletRequest,
            HttpServletResponse servletResponse, HttpResponse proxyResponse, int statusCode)
        throws ServletException, IOException {
        // Check if the proxy response is a redirect
        // The following code is adapted from org.tigris.noodle.filters.CheckForRedirect
        if (statusCode >= HttpServletResponse.SC_MULTIPLE_CHOICES /* 300 */
                && statusCode < HttpServletResponse.SC_NOT_MODIFIED /* 304 */) {
            Header locationHeader = proxyResponse.getLastHeader(HttpHeaders.LOCATION);
            if (locationHeader == null) {
                throw new ServletException("Received status code: " + statusCode
                        + " but no " + HttpHeaders.LOCATION + " header was found in the response");
            }
            // Modify the redirect to go to this proxy servlet rather that the proxied host
            String locStr = rewriteUrlFromResponse(servletRequest, locationHeader.getValue());

            servletResponse.sendRedirect(locStr);
            return true;
        }
        // 304 needs special handling.  See:
        // http://www.ics.uci.edu/pub/ietf/http/rfc1945.html#Code304
        // We get a 304 whenever passed an 'If-Modified-Since'
        // header and the data on disk has not changed; server
        // responds w/ a 304 saying I'm not going to send the
        // body because the file has not changed.
        if (statusCode == HttpServletResponse.SC_NOT_MODIFIED) {
            servletResponse.setIntHeader(HttpHeaders.CONTENT_LENGTH, 0);
            servletResponse.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return true;
        }
        return false;
    }

    private void closeQuietly(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
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
                }
                proxyRequest.addHeader(headerName, headerValue);
            }
        }
    }

    /**
     * Copy proxied response headers back to the servlet client.
     */
    private void copyResponseHeaders(HttpResponse proxyResponse,
            HttpServletResponse servletResponse) {
        for (Header header : proxyResponse.getAllHeaders()) {
            if (HOP_BY_HOP_HEADERS.containsHeader(header.getName())) {
                continue;
            }
            servletResponse.addHeader(header.getName(), header.getValue());
        }
    }

    /**
     * Copy response body data (the entity) from the proxy to the servlet client.
     */
    private void copyResponseEntity(HttpResponse proxyResponse, HttpServletResponse servletResponse)
        throws IOException {
        HttpEntity entity = proxyResponse.getEntity();
        if (entity != null) {
            OutputStream servletOutputStream = servletResponse.getOutputStream();
            try {
                entity.writeTo(servletOutputStream);
            } finally {
                closeQuietly(servletOutputStream);
            }
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
