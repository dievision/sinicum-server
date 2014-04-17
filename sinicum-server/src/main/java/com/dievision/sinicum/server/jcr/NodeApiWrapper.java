package com.dievision.sinicum.server.jcr;

import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface NodeApiWrapper {
    @JsonIgnore
    Node getNode();

    NodeApiWrapperMeta getMeta();

    Map<String, Object> getProperties() throws RepositoryException;

    Map<String, Object> getNodes() throws RepositoryException;
}
