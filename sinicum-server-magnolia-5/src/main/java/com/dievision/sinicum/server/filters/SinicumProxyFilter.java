package com.dievision.sinicum.server.filters;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.cms.filters.AbstractMgnlFilter;

import com.dievision.sinicum.server.mgnlAdapters.AggregationStateAdapter5;

public class SinicumProxyFilter extends AbstractMgnlFilter {
    private ProxyFilter proxyFilter;
    private static final Logger logger = LoggerFactory.getLogger(SinicumProxyFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        super.init(filterConfig);
        proxyFilter = new ProxyFilter(new AggregationStateAdapter5());
    }

    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        proxyFilter.doFilter(request, response, chain);
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
