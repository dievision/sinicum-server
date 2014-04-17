package com.dievision.sinicum.server.jaxrs.filters;

import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;

import com.dievision.sinicum.server.jaxrs.NotAllowedException;
import com.dievision.sinicum.server.mgnlAdapters.MgnlContextAdapter;
import com.dievision.sinicum.server.mgnlAdapters.SecurityAdapter;

public class LoginFilter implements ContainerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(LoginFilter.class);

    @Override
    public ContainerRequest filter(ContainerRequest request) {
        // String[] credentials = getCredentials(request);
        // try {
        //     performLogin(credentials[0], credentials[1]);
        // } catch (LoginException e) {
        //     throw new NotAllowedException("Invalid credentials.");
        // }
        MgnlContextAdapter.login(SecurityAdapter.getSystemSubject());
        return request;
    }

    private String[] getCredentials(ContainerRequest request) {
        String auth = request.getHeaderValue("authorization");
        if (auth == null) {
            throw new NotAllowedException("No authentication given");
        }
        String[] credentials = BasicAuth.decode(auth);
        if (credentials == null || credentials.length != 2) {
            throw new NotAllowedException("Invalid authentication");
        }
        return credentials;
    }

    private static class BasicAuth {
        public static String[] decode(String auth) {
            String[] result = null;

            //Replacing "Basic THE_BASE_64" to "THE_BASE_64" directly
            auth = auth.replaceFirst("[B|b]asic ", "");

            byte[] decodedBytes = DatatypeConverter.parseBase64Binary(auth);
            if (decodedBytes != null && decodedBytes.length != 0) {
                result = new String(decodedBytes).split(":", 2);
            }
            return result;
        }
    }
}
