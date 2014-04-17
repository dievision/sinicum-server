package com.dievision.sinicum.server.jcr;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class NodeApiWrapper5Meta extends NodeApiWrapper4Meta {
    private static final String MIXIN_LAST_MODIFIED = "mgnl:lastModified";
    private static final String MIXIN_ACTIVATABLE = "mgnl:activatable";
    private static final String MIXIN_RENDERABLE = "mgnl:renderable";
    private static final String MIXIN_CREATED = "mgnl:created";
    private static final String MIXIN_VERSIONABLE = "mgnl:versionable";
    private static final Logger logger = LoggerFactory.getLogger(NodeApiWrapper5Meta.class);

    public NodeApiWrapper5Meta(Node node) {
        super(node);
    }


    @JsonProperty("mgnl:lastModified")
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public String getMgnlLastModified() throws RepositoryException {
        return propertyForMixinType(MIXIN_LAST_MODIFIED, "mgnl:lastModified");
    }

    @JsonProperty("mgnl:lastModifiedBy")
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public String getMgnlLastModifiedBy() throws RepositoryException {
        return propertyForMixinType(MIXIN_LAST_MODIFIED, "mgnl:lastModifiedBy");
    }

    @JsonProperty("mgnl:template")
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public String getMgnlTemplate() throws RepositoryException {
        return propertyForMixinType(MIXIN_RENDERABLE, "mgnl:template");
    }

    @JsonProperty("mgnl:lastActivated")
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public String getMgnlLastActivated() throws RepositoryException {
        return propertyForMixinType(MIXIN_ACTIVATABLE, "mgnl:lastActivated");
    }

    @JsonProperty("mgnl:lastActivatedBy")
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public String getMgnlLastActivatedBy() throws RepositoryException {
        return propertyForMixinType(MIXIN_ACTIVATABLE, "mgnl:lastActivatedBy");
    }

    @JsonProperty("mgnl:activationStatus")
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public String getMgnlActivationStatus() throws RepositoryException {
        return propertyForMixinType(MIXIN_ACTIVATABLE, "mgnl:activationStatus");
    }

    @JsonProperty("mgnl:created")
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public String getMgnlCreated() throws RepositoryException {
        return propertyForMixinType(MIXIN_CREATED, "mgnl:created");
    }

    @JsonProperty("mgnl:createdBy")
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public String getMgnlCreatedBy() throws RepositoryException {
        return propertyForMixinType(MIXIN_CREATED, "mgnl:createdBy");
    }

    @JsonProperty("mgnl:comment")
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public String getMgnlComment() throws RepositoryException {
        return propertyForMixinType(MIXIN_VERSIONABLE, "mgnl:comment");
    }

    private String propertyForMixinType(String mixinType, String propertyName)
        throws RepositoryException {
        String lastModified = null;
        if (getNode().isNodeType(mixinType) && getNode().hasProperty(propertyName)) {
            lastModified = getNode().getProperty(propertyName).getString();
        }
        return lastModified;
    }
}
