package com.dievision.sinicum.server.resources.providers;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import com.dievision.sinicum.server.jaxrs.filters.PrettyPrintContext;

@Provider
public class SinicumObjectProvider implements ContextResolver<ObjectMapper> {
    private final ObjectMapper defaultObjectMapper;
    private final ObjectMapper prettyPrintObjectMapper;
    private static final JsonFactory JASON_FACTORY = new JsonFactory();
    private static final Logger logger = LoggerFactory.getLogger(SinicumObjectProvider.class);

    public SinicumObjectProvider() {
        this.defaultObjectMapper = createDefaultObjectMapper();
        this.prettyPrintObjectMapper = createPrettyPrintObjectMapper();
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        if (PrettyPrintContext.getInstance().getPrettyPrint()) {
            return prettyPrintObjectMapper;
        } else {
            return defaultObjectMapper;
        }
    }

    private ObjectMapper createDefaultObjectMapper() {
        ObjectMapper mapper = new ObjectMapper(JASON_FACTORY);
        return mapper;
    }

    private ObjectMapper createPrettyPrintObjectMapper() {
        ObjectMapper mapper = createDefaultObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        return mapper;
    }
}
