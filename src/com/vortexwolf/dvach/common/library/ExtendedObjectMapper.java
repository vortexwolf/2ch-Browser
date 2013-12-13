package com.vortexwolf.dvach.common.library;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;

public class ExtendedObjectMapper extends ObjectMapper {

    public ExtendedObjectMapper() {
        this.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
}
