package com.vortexwolf.chan.boards.makaba.models;

import org.codehaus.jackson.annotate.JsonProperty;

public class MakabaPostInfo {
    @JsonProperty("num")
    public String num;

    @JsonProperty("comment")
    public String comment;
    
    @JsonProperty("subject")
    public String subject;

    @JsonProperty("name")
    public String name;
    
    @JsonProperty("email")
    public String email;
    
    @JsonProperty("timestamp")
    public long timestamp;
    
    @JsonProperty("parent")
    public String parent;
    
    @JsonProperty("date")
    public String date;
    
    @JsonProperty("files")
    public MakabaFileInfo[] files;
    
    @JsonProperty("trip")
    public String trip;
    
    @JsonProperty("op")
    public int op;
}
