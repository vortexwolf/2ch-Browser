package ua.in.quireg.chan.boards.makaba.models;

import org.codehaus.jackson.annotate.JsonProperty;

public class MakabaThreadsList {
    @JsonProperty("threads")
    public MakabaThreadInfo[] threads;
    
    @JsonProperty("enable_icons")
    public int enable_icons;
    
    @JsonProperty("icons")
    public MakabaIconInfo[] icons;
}
