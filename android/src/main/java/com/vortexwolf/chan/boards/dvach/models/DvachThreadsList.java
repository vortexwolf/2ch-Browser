package com.vortexwolf.chan.boards.dvach.models;

import org.codehaus.jackson.annotate.JsonProperty;

public class DvachThreadsList {
    @JsonProperty("threads")
    public DvachThreadInfo[] threads;
}
