package com.dievision.sinicum.server.jcr;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyToJsonTypeTranslator {
    WysiwygTemplateTranslator templateTranslator = new WysiwygTemplateTranslator();

    private static final WysiwygTemplateTranslator TRANSLATOR = new WysiwygTemplateTranslator();
    private static final Logger logger =
            LoggerFactory.getLogger(PropertyToJsonTypeTranslator.class);

    public Object resolvePropertyToJsonType(Property prop) throws RepositoryException {
        Object propertyValue;
        if (!prop.isMultiple()) {
            propertyValue = resolvePropertyTypes(prop.getValue());
        } else {
            propertyValue = resolveMultiplePropertyTypes(prop.getValues());
        }
        return propertyValue;
    }

    private Object resolveMultiplePropertyTypes(Value[] values) throws RepositoryException {
        List<Object> result = new ArrayList<Object>(values.length);
        for (Value value : values) {
            result.add(resolvePropertyTypes(value));
        }
        return result;
    }

    private Object resolvePropertyTypes(Value value) throws RepositoryException {
        int type = value.getType();
        Object result;
        if (type == PropertyType.STRING) {
            result = templateTranslator.translate(value.getString());
        } else if (type == PropertyType.DOUBLE) {
            result = value.getDouble();
        } else if (type == PropertyType.LONG) {
            result = value.getLong();
        } else if (type == PropertyType.BOOLEAN) {
            result = value.getBoolean();
        } else if (type == PropertyType.DATE) {
            result = value.getString();
        } else if (type == PropertyType.BINARY) {
            result = "Binary Data Type not supported";
        } else {
            result = value.getString();
        }
        return result;
    }
}
