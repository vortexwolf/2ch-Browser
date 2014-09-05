package com.vortexwolf.chan.boards.makaba.models;

import org.codehaus.jackson.annotate.JsonProperty;

public class MakabaThreadsList {
    @JsonProperty("threads")
    public MakabaThreadInfo[] threads;
    
    @JsonProperty("enable_icons")
    public int enable_icons;
    
    @JsonProperty("icons")
    public MakabaIconInfo[] icons;
}
