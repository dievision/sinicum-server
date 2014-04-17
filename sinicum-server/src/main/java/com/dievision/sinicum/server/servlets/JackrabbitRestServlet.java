package com.dievision.sinicum.server.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class JackrabbitRestServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(JackrabbitRestServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
        resp.setStatus(200);
    }
}
