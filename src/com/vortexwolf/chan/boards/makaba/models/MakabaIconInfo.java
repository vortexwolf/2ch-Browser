package com.vortexwolf.chan.boards.makaba.models;

import org.codehaus.jackson.annotate.JsonProperty;

public class MakabaIconInfo {
    @JsonProperty("name")
    public String name;
    
    @JsonProperty("num")
    public int num;

    @JsonProperty("url")
    public String url;
}
