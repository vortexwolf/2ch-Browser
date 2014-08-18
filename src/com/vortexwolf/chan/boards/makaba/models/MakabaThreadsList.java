package com.vortexwolf.chan.boards.makaba.models;

import org.codehaus.jackson.annotate.JsonProperty;

public class MakabaThreadsList {
    @JsonProperty("threads")
    public MakabaThreadInfo[] threads;
}
