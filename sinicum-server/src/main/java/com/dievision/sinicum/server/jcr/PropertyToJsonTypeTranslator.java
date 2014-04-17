package com.dievision.sinicum.server.jcr;

import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyToJsonTypeTranslator {
    private static final WysiwygTemplateTranslator TRANSLATOR = new WysiwygTemplateTranslator();
    private static final Logger logger =
            LoggerFactory.getLogger(PropertyToJsonTypeTranslator.class);

    public Object resolvePropertyToJsonType(Property property) throws RepositoryException {
        Object result = null;
        int type = property.getType();
        if (type == PropertyType.STRING) {
            result = TRANSLATOR.translate(property.getString());
        } else if (type == PropertyType.DOUBLE) {
            result = property.getDouble();
        } else if (type == PropertyType.LONG) {
            result = property.getLong();
        } else if (type == PropertyType.BOOLEAN) {
            result = property.getBoolean();
        } else if (type == PropertyType.DATE) {
            result = property.getString();
        } else if (type == PropertyType.BINARY) {
            result = null;
        } else if (!property.getDefinition().isMultiple()) {
            result = property.getString();
        }
        return result;
    }

}
