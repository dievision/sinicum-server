package com.dievision.sinicum.server.jaxrs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

/**
 *
 */
public final class JaxRsExceptionHelper {
    private static final Logger logger = LoggerFactory.getLogger(JaxRsExceptionHelper.class);

    private JaxRsExceptionHelper() {
        // nothing
    }

    protected static String formatMessage(String message) {
        String jsonMessage = "";
        JsonFactory json = new JsonFactory();
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            JsonGenerator gen = json.createJsonGenerator(outputStream , JsonEncoding.UTF8);
            gen.writeStartObject();
            gen.writeObjectField("message", message);
            gen.writeEndObject();
            gen.close();
            jsonMessage = outputStream.toString("UTF-8");
        } catch (IOException e) {
            logger.error("Error writing to JSON Object: " + e.toString());
        }
        return jsonMessage;
    }
}
