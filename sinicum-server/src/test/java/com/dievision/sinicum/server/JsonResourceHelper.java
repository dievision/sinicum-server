package com.dievision.sinicum.server;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.dievision.sinicum.server.jcr.templating.DialogResolver;

public final class JsonResourceHelper {
    private static final JsonFactory JSON_FACTORY = new JsonFactory();
    private static final Logger logger = LoggerFactory.getLogger(JsonResourceHelper.class);

    private JsonResourceHelper() {
    }

    public static Map resourceObjectToJsonMap(DialogResolver resolver) throws IOException {
        ObjectMapper mapper = new ObjectMapper(JSON_FACTORY);
        String result = mapper.writeValueAsString(resolver);
        System.out.println(result);

        JsonParser parser = JSON_FACTORY.createJsonParser(result);
        return parser.readValueAs(Map.class);
    }
}
