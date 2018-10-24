package com.dievision.sinicum.server.mgnlAdapters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.magnolia.context.MgnlContext;

public class AggregationStateAdapter5 implements AggregationStateAdapter {
    private static final Logger logger = LoggerFactory.getLogger(AggregationStateAdapter5.class);

    @Override
    public boolean isPreviewMode() {
        return MgnlContext.getAggregationState().isPreviewMode();
    }
}
