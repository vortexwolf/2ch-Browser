package com.vortexwolf.chan.boards.dvach.models;

import org.codehaus.jackson.annotate.JsonProperty;

public class DvachPostInfo {
    @JsonProperty("num")
    public String num;
    
    @JsonProperty("thumbnail")
    public String thumbnail;
    
    @JsonProperty("comment")
    public String comment;
    
    @JsonProperty("subject")
    public String subject;

    @JsonProperty("video")
    public String video;
    
    @JsonProperty("image")
    public String image;
    
    @JsonProperty("size")
    public int size;
    
    @JsonProperty("name")
    public String name;
    
    @JsonProperty("width")
    public int width;
    
    @JsonProperty("height")
    public int height;
    
    @JsonProperty("timestamp")
    public long timestamp;
    
    @JsonProperty("parent")
    public String parent;
    
    @JsonProperty("date")
    public String date;
    
    @JsonProperty("postername")
    public String postername; // from m2-ch search
}
