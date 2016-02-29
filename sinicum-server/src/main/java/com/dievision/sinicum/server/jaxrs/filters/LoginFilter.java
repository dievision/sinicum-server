package com.dievision.sinicum.server.jaxrs.filters;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.xml.bind.DatatypeConverter;

import org.glassfish.jersey.server.ContainerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dievision.sinicum.server.jaxrs.NotAllowedException;
import com.dievision.sinicum.server.mgnlAdapters.MgnlContextAdapter;
import com.dievision.sinicum.server.mgnlAdapters.SecurityAdapter;

public class LoginFilter implements ContainerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(LoginFilter.class);

    public void filter(ContainerRequestContext requestContext) throws IOException {
        MgnlContextAdapter.login(SecurityAdapter.getSystemSubject());
    }

    private String[] getCredentials(ContainerRequest request) {
        String auth = request.getHeaderString("authorization");
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
