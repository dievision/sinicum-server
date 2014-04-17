package com.dievision.sinicum.server.jcr;

import java.util.List;

import javax.jcr.RepositoryException;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public interface NodeApiWrapperMeta {
    String getWorkspace() throws RepositoryException;

    @JsonProperty("jcr:uuid")
    String getJcrUuid() throws RepositoryException;

    String getPath() throws RepositoryException;

    String getName() throws RepositoryException;

    @JsonProperty("jcr:primaryType")
    String getJcrPrimaryType() throws RepositoryException;

    List<String> getSuperTypes() throws RepositoryException;

    int getDepth() throws RepositoryException;

    @JsonProperty("jcr:created")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String getJcrCreated() throws RepositoryException;

    List<String> getMixinNodeTypes() throws RepositoryException;
}
