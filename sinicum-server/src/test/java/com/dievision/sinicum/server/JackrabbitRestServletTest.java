package com.dievision.sinicum.server;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;

import com.dievision.sinicum.server.servlets.JackrabbitRestServlet;

import static org.junit.Assert.*;

/**
 *
 */
public class JackrabbitRestServletTest {
    private static final Logger logger = LoggerFactory.getLogger(JackrabbitRestServletTest.class);
    ServletUnitClient client = null;

    @Before
    public void setup() {
        ServletRunner sr = new ServletRunner();
        sr.registerServlet("jackrabbitServlet/*", JackrabbitRestServlet.class.getName());
        client = sr.newClient();
    }

    @Test
    public void testGetRequest() throws IOException, SAXException {
        WebRequest request = new GetMethodWebRequest("http://something/jackrabbitServlet/hierwas");
        WebResponse response = client.getResponse(request);
        assertNotNull(response);
    }
}
